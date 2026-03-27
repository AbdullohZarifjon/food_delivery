package com.example.food.service.impl;

import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.entity.*;
import com.example.food.entity.enums.OrderStatus;
import com.example.food.entity.enums.UserRole;
import com.example.food.exception.BadRequestException;
import com.example.food.exception.ForbiddenException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.LocationMapper;
import com.example.food.mapper.OrderMapper;
import com.example.food.repo.BranchRepository;
import com.example.food.repo.OrderRepository;
import com.example.food.service.*;
import com.example.food.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final GeometryFactory geometryFactory;
    private final OrderItemService orderItemService;
    private final BranchService branchService;
    private final BranchRepository branchRepository;
    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationMapper locationMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, GeometryFactory geometryFactory, OrderItemService orderItemService, BranchService branchService, BranchRepository branchRepository, NotificationService notificationService, RedisTemplate<String, Object> redisTemplate, LocationMapper locationMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.geometryFactory = geometryFactory;
        this.orderItemService = orderItemService;
        this.branchService = branchService;
        this.branchRepository = branchRepository;
        this.notificationService = notificationService;
        this.redisTemplate = redisTemplate;
        this.locationMapper = locationMapper;
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor createOrder(OrderDto.OrderRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
//        Branch branch = branchService.getMainBranch(); // 1. Restoranni topamiz

        // 2. Mijoz koordinatasi
        Point customerLoc = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        customerLoc.setSRID(4326);

        // 3. Masofani hisoblash
//        Double distanceInMeters = branchRepository.calculateDistance(branch.getLocation(), customerLoc);
        double distanceInMeters = 1.2;
        BigDecimal deliveryFee = branchService.calculateDeliveryFee(distanceInMeters);

        // 4. Order yaratish
        Order order = Order.builder()
                .orderCode(generateUniqueOrderCode())
                .customer(currentUser)
                .username(currentUser.getUsername())
                .customerName(currentUser.getName())
                .contactPhone(request.contactPhone())
                .deliveryAddressName(request.deliveryAddressName())
                .deliveryAddress(customerLoc)
                .deliveryFee(deliveryFee) // Dostavka narxi
                .distance(distanceInMeters / 1000.0) // KM ga o'tkazib saqlaymiz
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = orderItemService.createOrderItems(order, request.items());

        BigDecimal itemsTotal = orderItems.stream()
                .map(OrderItem::getRowTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(orderItems);
        order.setTotalPrice(itemsTotal.add(deliveryFee)); // Jami: Ovqat + Dostavka

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        notificationService.sendOrderNotification(response, savedOrder.getUsername());

        log.info("Yangi buyurtma yaratildi va notification yuborildi: {}", savedOrder.getOrderCode());
        return response;
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor confirmOrder(UUID id) {
        // 1. Buyurtmani topamiz
        Order order = findById(id);

        // 2. Faqat PENDING bo'lgan buyurtmani tasdiqlash mumkin
        checkOrderStatus(order, OrderStatus.PENDING);

        // 3. Statusni o'zgartiramiz
        order.setStatus(OrderStatus.PREPARING);

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        // 4. WebSocket orqali hamma manfaatdor tomonlarga (Admin, Mijoz) xabar yuboramiz
        // Bu yerda "/topic/orders" kanaliga yangilangan statusni otib yuboramiz
        notificationService.sendOrderNotification(response, savedOrder.getUsername());

        log.info("Buyurtma #{} admin tomonidan tasdiqlandi. Status: PREPARING", order.getOrderCode());
        return response;
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor readyOrder(UUID id) {
        // 1. Buyurtmani topish
        Order order = findById(id);

        // 2. Faqat PREPARING holatidagi buyurtmani TAYYOR deb belgilash mumkin
        checkOrderStatus(order, OrderStatus.PREPARING);

        // 3. Statusni yangilash
        order.setStatus(OrderStatus.READY_TO_PICKUP);

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        // 4. Notification yuborish
        // - Admin va Kurerlar monitorida buyurtma "Tayyor" bo'limiga o'tadi
        // - Mijozga "Buyurtmangiz tayyor, kurerga topshirilmoqda" xabari boradi
        notificationService.sendOrderNotification(response, savedOrder.getUsername());

        log.info("Buyurtma #{} tayyor bo'ldi. Kurerlar qabul qilishi mumkin.", order.getOrderCode());
        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> getUserActiveOrders() {
        User currentUser = SecurityUtils.getCurrentUser();

        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY_TO_PICKUP,
                OrderStatus.ACCEPTED,
                OrderStatus.ON_THE_WAY // Kurer yo'lda bo'lsa ham aktiv hisoblanadi
        );

        return orderRepository.findAllByCustomerIdAndStatusInAndDeletedFalse(currentUser.getId(), activeStatuses)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponseForMonitor> getActiveOrders() {
        // Statuslar ro'yxati
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY_TO_PICKUP,
                OrderStatus.ACCEPTED
        );

        return orderRepository.findAllByStatusInOrderByCreatedAtDesc(activeStatuses)
                .stream()
                .map(orderMapper::toResponseForMonitor)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor rejectOrder(UUID id) {
        // 1. Buyurtmani topamiz
        Order order = findById(id);

        // 2. Xavfsizlik tekshiruvi: Faqat ACCEPTED holatidagi buyurtmadan voz kechish mumkin
        // Agar kurer allaqachon yo'lga chiqqan (DELIVERING) bo'lsa, voz kecha olmaydi (qoida bo'yicha)
        checkOrderStatus(order, OrderStatus.ACCEPTED);

        // 3. Tekshiruv: Buyurtmani aynan o'sha biriktirilgan kurer rad etayotganini aniqlaymiz
        User currentUser = SecurityUtils.getCurrentUser();
        if (order.getCourier() == null || !order.getCourier().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Siz bu buyurtmaga biriktirilmagansiz, voz kecha olmaysiz!");
        }

        // 4. Kurer ma'lumotlarini tozalaymiz (Order yana ochiq bo'lishi uchun)
        order.setCourier(null);
        order.setCourierName(null);
        order.setCourierPhoneAtOrder(null);

        // 5. Statusni qaytadan "Kurer kutilyapti" holatiga o'tkazamiz
        order.setStatus(OrderStatus.READY_TO_PICKUP);

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        // 6. SSE orqali xabardor qilish:
        // - Boshqa kurerlar monitorida bu buyurtma qaytadan "Aktiv" bo'lib ko'rinadi
        // - Mijozga "Kurer voz kechdi, yangi kurer qidirilyapti" xabari boradi
        notificationService.sendOrderNotification(response, order.getCustomer().getUsername());

        log.info("Buyurtma #{} kurer {} tomonidan rad etildi va qayta READY_TO_PICKUP holatiga o'tdi.",
                order.getOrderCode(), currentUser.getName());

        return response;
    }

    @Override
    @Transactional(readOnly = true) // O'qish uchun tranzaksiya, Senior!
    public List<OrderDto.OrderResponse> getCourierActiveOrders() {
        // 1. Joriy kurerning ma'lumotlarini olamiz
        User currentUser = SecurityUtils.getCurrentUser();

        // 2. Kurerga tegishli faol statuslar (Hali mijozga yetib bormaganlar)
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.ACCEPTED,   // Kurer zakazni oldi, lekin hali restorandan chiqmagan bo'lishi mumkin
                OrderStatus.ON_THE_WAY  // Kurer yo'lda, mijozga qarab ketyapti
        );

        // 3. Bazadan qidirish (User ID bo'yicha emas, Courier obyekti bo'yicha)
        return orderRepository.findAllByCourierUserIdAndStatusInAndDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(),
                        activeStatuses
                ).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto.OrderResponse getByOrderCode(String orderCode) {
        Order order = findByOrderCode(orderCode);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> getCourierOrderHistoryByDate(LocalDate date) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Agar sana berilmagan bo'lsa, bugungi sana olinadi
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 2. Kun boshlanishi (00:00:00) va tugashi (23:59:59)
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        // 3. Bazadan filtrlab olish
        return orderRepository.findAllByCourierUserIdAndStatusAndCreatedAtBetweenAndDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(),
                        OrderStatus.DELIVERED,
                        startOfDay,
                        endOfDay
                ).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markOrdersAsOnTheWay(UUID courierId) {
        List<Order> acceptedOrders = getOrdersByCourierIdAndStatus(courierId, OrderStatus.ACCEPTED);

        if (acceptedOrders.isEmpty()) {
            throw new BadRequestException("Sizda hali qabul qilingan buyurtmalar mavjud emas!");
        }

        // 1. Statuslarni o'zgartirish va foydalanuvchi nomlarini yig'ish
        List<String> usernames = new ArrayList<>();
        acceptedOrders.forEach(order -> {
            order.setStatus(OrderStatus.ON_THE_WAY);
            usernames.add(order.getUsername());

        // --- NOTIFICATION QISMI ---
        // Mijozga va Adminga buyurtma yo'lda ekanini bildiramiz
        // orderMapper.toResponse(order) orqali DTO yasaymiz
        notificationService.sendOrderNotification(
                orderMapper.toResponseForMonitor(order),
                order.getUsername()
        );
    });

        // 2. Bazada yangilaymiz
        orderRepository.saveAll(acceptedOrders);

        // 3. Keshga yuklash: courierId ga bog'langan holda 10 daqiqaga
        String cacheKey = "courier_active_clients:" + courierId;
        redisTemplate.opsForValue().set(cacheKey, usernames, 10, TimeUnit.MINUTES);

        log.info("Kuryer {} uchun {} ta mijoz keshga yuklandi (10 min)", courierId, usernames.size());
    }

    private List<Order> getOrdersByCourierIdAndStatus(UUID courierId, OrderStatus status) {
        return orderRepository.findAllByCourierIdAndStatus(
                courierId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public void broadcastCourierLocation(LocationDto.CourierLocationRequest locationRequest) {
        UUID courierId = SecurityUtils.getCurrentUserId();

        // 1. Kuryerga tegishli faol mijozlarni (usernames) olamiz
        List<String> activeUsernames = getActiveClientUsernames(courierId);

        if (activeUsernames.isEmpty()) {
            return; // Aktiv zakaz bo'lmasa, lokatsiya tarqatish shart emas
        }

        // 2. Mapper orqali response yasaymiz
        LocationDto.LocationResponse response = locationMapper.toResponse(locationRequest);

        // 3. [YANGI] Kuryerning oxirgi nuqtasini Redisga saqlaymiz
        saveLastLocation(courierId, response);

        // 4. Barcha bog'liq mijozlarga SSE orqali yuboramiz
        activeUsernames.forEach(username ->
                notificationService.sendLocationUpdate(username, response)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDto.LocationResponse getCourierLastLocation(UUID orderId) {
        // 1. Orderni topamiz
        Order order = findById(orderId);

        // 2. Buyurtma holatini tekshiramiz (faqat yo'ldagilar uchun lokatsiya beriladi)
        if (order.getStatus() != OrderStatus.ON_THE_WAY) {
            throw new BadRequestException("Kuryer hali yo'lga chiqmagan yoki buyurtma yakunlangan");
        }

        if (order.getCourier() == null) {
            throw new BadRequestException("Buyurtmaga kuryer biriktirilmagan");
        }

        // 3. Redisdan kuryerning oxirgi lokatsiyasini qidiramiz
        String locationKey = "courier_last_location:" + order.getCourier().getId();
        LocationDto.LocationResponse lastLocation =
                (LocationDto.LocationResponse) redisTemplate.opsForValue().get(locationKey);

        // 4. Agar kuryer hali birorta ham nuqta yubormagan bo'lsa (Redis bo'sh bo'lsa)
        if (lastLocation == null) {
            throw new RecordNotFoundException("Kuryer hali yurishga ulgurmadi!");
        }

        return lastLocation;
    }


    @Override
    @Transactional
    public void processOrderCompletion(UUID orderId, LocationDto.CourierLocationRequest courierLocation, UUID courierId) {
        Order order = findById(orderId);

        // 1. Status check
        if (order.getStatus() != OrderStatus.ON_THE_WAY) {
            throw new BadRequestException("Buyurtma yo'lda emas!");
        }

        // Point dan koordinatalarni olamiz: PostGISda X = Longitude, Y = Latitude
        double destLon = order.getDeliveryAddress().getX();
        double destLat = order.getDeliveryAddress().getY();

        // Masofani hisoblaymiz
        double distance = calculateDistance(
                courierLocation.latitude(), courierLocation.longitude(),
                destLat, destLon
        );

        if (distance > 200) {
            throw new BadRequestException(String.format("Mijozdan uzoqdasiz: %.0f metr", distance));
        }

        // 3. Buyurtmani yopish
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        redisTemplate.delete(List.of(
                "courier_active_clients:" + courierId,
                "courier_last_location:" + courierId));

        notificationService.sendOrderNotification(orderMapper.toResponseForMonitor(order), order.getUsername());
    }


    @Override
    public OrderDto.OrderResponseForMonitor acceptOrderByCourier(UUID orderId, Courier courier, User currentUser) {
        // 1. Buyurtmani bazadan qulflash (Pessimistic Locking tavsiya etiladi)
        Order order = findById(orderId);

        // 2. Xavfsizlik: Faqat READY_TO_PICKUP holatidagini olish mumkin
        checkOrderStatus(order, OrderStatus.READY_TO_PICKUP);


        // 4. Kurer ma'lumotlarini Snapshot qilish (Order ichiga saqlash)
        order.setCourier(courier);
        order.setCourierName(currentUser.getName());
        order.setCourierPhoneAtOrder(currentUser.getPhoneNumber());

        // 5. Statusni o'zgartirish
        order.setStatus(OrderStatus.ACCEPTED);

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        // 6. Hammani xabardor qilish:
        // - Admin monitorida: "Kurer falonchi zakazni oldi"
        // - Boshqa kurerlarda: Bu zakaz ro'yxatdan yo'qoladi (ACCEPTED bo'lgani uchun)
        // - Mijozda: "Kurer yo'lga chiqdi, ismi: Falonchi"
        notificationService.sendOrderNotification(response, savedOrder.getUsername());

        log.info("Buyurtma #{} kurer {} tomonidan qabul qilindi.", order.getOrderCode(), currentUser.getName());
        return response;
    }

    /**
     * Kuryerning oxirgi turgan joyini Redisda saqlash (30 minut davomida)
     */
    private void saveLastLocation(UUID courierId, LocationDto.LocationResponse response) {
        String locationKey = "courier_last_location:" + courierId;
        // 30 minut muddat yetarli, kuryer yangi nuqta yuborsa bu vaqt yana yangilanadi
        redisTemplate.opsForValue().set(locationKey, response, 30, TimeUnit.MINUTES);
    }

    private List<String> getActiveClientUsernames(UUID courierId) {
        String cacheKey = "courier_active_clients:" + courierId;

        // Redisdan cast qilishda ehtiyot bo'lamiz
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData instanceof List) {
            return (List<String>) cachedData;
        }

        // DB'dan olish
        List<Order> activeOrders = getOrdersByCourierIdAndStatus(courierId, OrderStatus.ON_THE_WAY);

        List<String> usernames = activeOrders.stream()
                .map(Order::getUsername)
                .collect(Collectors.toList());

        if (!usernames.isEmpty()) {
            // Siz aytgan 10 minutlik muddat
            redisTemplate.opsForValue().set(cacheKey, usernames, 10, TimeUnit.MINUTES);
        }

        return usernames;
    }


    // 1. Admin uchun metod
    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor updateOrderItemsByAdmin(UUID orderId, List<OrderDto.OrderItemRequest> newItemsRequest) {
        Order order = findById(orderId);

        // Admin xohlagan paytda o'zgartirishi mumkin, lekin DELIVERED bo'lganini teginmagani ma'qul
        if (order.getStatus() == OrderStatus.ON_THE_WAY || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Yo'ldagi yoki yetkazilgan buyurtmani o'zgartirib bo'lmaydi!");
        }

        return performUpdate(order, newItemsRequest);
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor cancelOrderByAdmin(UUID id, String reason) {
        // 1. Buyurtmani kod orqali topamiz
        Order order = findById(id);

        // 2. Statusni tekshiramiz (Allaqlon yetkazilgan yoki bekor bo'lgan bo'lsa xato beramiz)
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Ushbu buyurtmani bekor qilib bo'lmaydi. Status: " + order.getStatus());
        }

        // 3. Bekor qilish haqida ma'lumotlarni to'ldiramiz
        User currentAdmin = SecurityUtils.getCurrentUser();

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledByName(currentAdmin.getName());
        order.setCancelledByRole(UserRole.ROLE_ADMIN);
        order.setCancellationReason(reason);

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        try {
            notificationService.sendOrderNotification(response, order.getUsername());
        } catch (Exception e) {
            log.error("Notification yuborishda xatolik (#{}): {}", order.getOrderCode(), e.getMessage());
        }

        log.warn("Admin {} buyurtmani bekor qildi (#{}). Sabab: {}",
                currentAdmin.getUsername(), order.getOrderCode(), reason);

        return response;
    }


    // 2. Mijoz uchun mavjud metodni yangilaymiz
    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor updateOrderItems(UUID orderId, List<OrderDto.OrderItemRequest> newItemsRequest) {
        Order order = findById(orderId);

        // Mijoz uchun qat'iy tekshiruv
        checkOrderStatus(order, OrderStatus.PENDING);

        User currentUser = SecurityUtils.getCurrentUser();
        if (!order.getCustomer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bu sizning buyurtmangiz emas!");
        }

        return performUpdate(order, newItemsRequest);
    }

    // 3. ASOSIY MANTIQ (Internal Logic)
    private OrderDto.OrderResponseForMonitor performUpdate(Order order, List<OrderDto.OrderItemRequest> newItemsRequest) {

        // Yangi itemlarni yaratish
        List<OrderItem> newOrderItems = orderItemService.createOrderItems(order, newItemsRequest);

        // Bu xato tranzaksiyani to'xtatadi va bazadagi hech nima o'zgarmaydi
        if (newOrderItems == null || newOrderItems.isEmpty()) {
            log.error("Update failed: New items list is empty for order {}", order.getId());
            throw new BadRequestException("Buyurtma tarkibi bo'sh bo'lishi mumkin emas!");
        }

        // 3. Tozalash va qo'shish (Faqat xato bo'lmasa bu yerga yetib keladi)
        // orphanRemoval=true bo'lgani uchun eski itemlar bazadan ham o'chadi
        order.getItems().clear();
        order.getItems().addAll(newOrderItems);

        // Narxni qayta hisoblash
        BigDecimal itemsTotal = newOrderItems.stream()
                .map(OrderItem::getRowTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(itemsTotal.add(order.getDeliveryFee()));

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        // SSE orqali hammani (Mijozni ham, Adminni ham) xabardor qilish
        notificationService.sendOrderNotification(response, savedOrder.getUsername());

        return response;
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor cancelOrderByCustomer(UUID id, String reason) {
        // 1. Buyurtmani topamiz
        Order order = findById(id);

        // 2. Xavfsizlik: Bu buyurtma haqiqatdan ham shu mijoznikimi?
        User currentUser = SecurityUtils.getCurrentUser();
        if (!order.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Siz faqat o'z buyurtmalaringizni bekor qilishingiz mumkin!");
        }

        // 3. Biznes qoidasi: Faqat PENDING yoki PREPARING holatida bekor qilish mumkin
        // Agar kurer buyurtmani olgan bo'lsa (READY_TO_PICKUP, ON_THE_WAY), bekor qilib bo'lmaydi
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARING) {
            throw new BadRequestException("Buyurtmani bu holatda bekor qilib bo'lmaydi. Kurer allaqachon yo'lga chiqqan bo'lishi mumkin.");
        }

        // 4. Ma'lumotlarni muhrlaymiz
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledByName(currentUser.getName());
        order.setCancelledByRole(UserRole.ROLE_USER); // UserRole enumidan
        order.setCancellationReason(reason);
        order.setCancelledByName(currentUser.getName());

        Order savedOrder = orderRepository.save(order);
        OrderDto.OrderResponseForMonitor response = orderMapper.toResponseForMonitor(savedOrder);

        // 5. Monitorga (Adminga/Restoranga) xabar beramiz
        // Muhim: Restoran ovqat tayyorlashni to'xtatishi kerak!
        try {
            notificationService.sendOrderNotification(response, order.getUsername());
        } catch (Exception e) {
            log.error("Mijoz bekor qilganda xabar yuborishda xato (#{}): {}", order.getOrderCode(), e.getMessage());
        }

        log.info("Mijoz {} buyurtmani bekor qildi (#{}). Sabab: {}",
                currentUser.getUsername(), order.getOrderCode(), reason);

        return response;
    }

    private Order findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCodeAndDeletedFalse(orderCode)
                .orElseThrow(() -> new RecordNotFoundException("Buyurtma topilmadi: " + orderCode));
    }

    private String generateUniqueOrderCode() {
        // 10,000 dan 99,999 gacha bo'lgan son (5 xonali)
        int number = ThreadLocalRandom.current().nextInt(10000, 100000);
        String code = String.valueOf(number);

        // Agar bazada bunday kod bo'lsa, qaytadan generatsiya qilamiz (Recursion)
        if (orderRepository.existsByOrderCode(code)) {
            return generateUniqueOrderCode();
        }
        return code;
    }

    private Order findById(UUID id) {
        return orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RecordNotFoundException("Buyurtma topilmadi"));
    }

    private void checkOrderStatus(Order order, OrderStatus status) {
        if (order.getStatus() != status) {
            throw new BadRequestException(
                    String.format("Amalni bajarish uchun buyurtma statusi '%s' bo'lishi kerak. Hozirgi status: '%s'",
                            status, order.getStatus())
            );
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Yer radiusi (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Masofa metrda
    }
}
