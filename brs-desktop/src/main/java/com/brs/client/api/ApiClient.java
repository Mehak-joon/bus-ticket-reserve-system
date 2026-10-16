package com.brs.client.api;

import com.brs.client.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static ApiClient instance;

    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private UserSession currentSession;

    private ApiClient() {
        this.baseUrl = DEFAULT_BASE_URL;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public UserSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(UserSession session) {
        this.currentSession = session;
    }

    public boolean isLoggedIn() {
        return currentSession != null && currentSession.isLoggedIn();
    }

    public boolean isAdmin() {
        return currentSession != null && currentSession.isAdmin();
    }

    public void logout() {
        this.currentSession = null;
    }

    private Request.Builder newAuthenticatedRequest(String url) {
        Request.Builder builder = new Request.Builder().url(url);
        if (isLoggedIn()) {
            builder.addHeader("Authorization", "Bearer " + currentSession.getToken());
        }
        return builder;
    }

    // -------------------------------------------------------------------------
    // AUTHENTICATION
    // -------------------------------------------------------------------------
    public UserSession login(String username, String password) throws IOException {
        Map<String, String> payload = Map.of(
                "username", username,
                "password", password
        );

        String jsonBody = objectMapper.writeValueAsString(payload);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/login")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String respStr = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(respStr);

            if (!response.isSuccessful() || !root.path("success").asBoolean(true)) {
                String errorMsg = root.path("message").asText("Invalid credentials");
                throw new IOException(errorMsg);
            }

            JsonNode data = root.path("data");
            UserSession session = UserSession.builder()
                    .id(data.path("id").asLong())
                    .username(data.path("username").asText())
                    .email(data.path("email").asText())
                    .fullName(data.path("fullName").asText())
                    .role(data.path("role").asText())
                    .token(data.path("token").asText())
                    .build();

            this.currentSession = session;
            return session;
        }
    }

    public void register(String username, String email, String password, String fullName, String phoneNumber) throws IOException {
        Map<String, String> payload = Map.of(
                "username", username,
                "email", email,
                "password", password,
                "fullName", fullName,
                "phoneNumber", phoneNumber
        );

        String jsonBody = objectMapper.writeValueAsString(payload);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/register")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String respStr = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(respStr);

            if (!response.isSuccessful() || !root.path("success").asBoolean(true)) {
                String errorMsg = root.path("message").asText("Registration failed");
                throw new IOException(errorMsg);
            }
        }
    }

    // -------------------------------------------------------------------------
    // CITIES & ROUTES
    // -------------------------------------------------------------------------
    public List<String> getSourceCities() throws IOException {
        Request request = new Request.Builder().url(baseUrl + "/api/routes/cities/sources").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<String>>() {});
        }
    }

    public List<String> getDestinationCities() throws IOException {
        Request request = new Request.Builder().url(baseUrl + "/api/routes/cities/destinations").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<String>>() {});
        }
    }

    // -------------------------------------------------------------------------
    // BUS SCHEDULE SEARCH & SEAT LAYOUT
    // -------------------------------------------------------------------------
    public List<ScheduleModel> searchSchedules(String source, String destination, LocalDate date) throws IOException {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl + "/api/schedules/search")).newBuilder();
        if (source != null && !source.isBlank()) urlBuilder.addQueryParameter("sourceCity", source);
        if (destination != null && !destination.isBlank()) urlBuilder.addQueryParameter("destinationCity", destination);
        if (date != null) urlBuilder.addQueryParameter("travelDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));

        Request request = new Request.Builder().url(urlBuilder.build()).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<ScheduleModel>>() {});
        }
    }

    public ScheduleModel getScheduleSeats(Long scheduleId) throws IOException {
        Request request = new Request.Builder().url(baseUrl + "/api/schedules/" + scheduleId + "/seats").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractObjectData(response, ScheduleModel.class);
        }
    }

    // -------------------------------------------------------------------------
    // SEAT LOCKING & BOOKING
    // -------------------------------------------------------------------------
    public void lockSeats(Long scheduleId, List<String> seatNumbers) throws IOException {
        Map<String, Object> payload = Map.of(
                "scheduleId", scheduleId,
                "seatNumbers", seatNumbers
        );

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/schedules/lock-seats").post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public BookingModel createBooking(Long scheduleId, List<PassengerModel> passengers) throws IOException {
        Map<String, Object> payload = Map.of(
                "scheduleId", scheduleId,
                "passengers", passengers
        );

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/bookings").post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            return extractObjectData(response, BookingModel.class);
        }
    }

    // -------------------------------------------------------------------------
    // PAYMENT (RAZORPAY TEST MODE)
    // -------------------------------------------------------------------------
    public Map<String, Object> createRazorpayOrder(Long bookingId) throws IOException {
        Map<String, Object> payload = Map.of("bookingId", bookingId);
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/payments/create-order").post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            return extractObjectData(response, new TypeReference<Map<String, Object>>() {});
        }
    }

    public void verifyPayment(Long bookingId, String orderId, String paymentId, String signature) throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("bookingId", String.valueOf(bookingId));
        payload.put("razorpayOrderId", orderId);
        payload.put("razorpayPaymentId", paymentId);
        payload.put("razorpaySignature", signature != null ? signature : "mock_signature");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/payments/verify").post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    // -------------------------------------------------------------------------
    // MY BOOKINGS & CANCELLATION
    // -------------------------------------------------------------------------
    public List<BookingModel> getMyBookings() throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/bookings/my-bookings").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<BookingModel>>() {});
        }
    }

    public BookingModel cancelBooking(Long bookingId, String reason) throws IOException {
        Map<String, String> payload = Map.of("reason", reason);
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/bookings/" + bookingId + "/cancel").post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            return extractObjectData(response, BookingModel.class);
        }
    }

    // -------------------------------------------------------------------------
    // TICKETS: PDF & QR
    // -------------------------------------------------------------------------
    public byte[] downloadPdfBytes(String ticketNumber) throws IOException {
        Request request = new Request.Builder().url(baseUrl + "/api/tickets/" + ticketNumber + "/pdf").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download PDF ticket (HTTP " + response.code() + ")");
            }
            return response.body() != null ? response.body().bytes() : new byte[0];
        }
    }

    public void savePdfToFile(String ticketNumber, File targetFile) throws IOException {
        byte[] bytes = downloadPdfBytes(ticketNumber);
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(bytes);
        }
    }

    public byte[] getQrCodeBytes(String ticketNumber) throws IOException {
        Request request = new Request.Builder().url(baseUrl + "/api/tickets/" + ticketNumber + "/qr").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to load QR code image (HTTP " + response.code() + ")");
            }
            return response.body() != null ? response.body().bytes() : new byte[0];
        }
    }

    // -------------------------------------------------------------------------
    // ADMIN DASHBOARD & MANAGEMENT
    // -------------------------------------------------------------------------
    public AdminStatsModel getAdminStats() throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/reports/dashboard").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractObjectData(response, AdminStatsModel.class);
        }
    }

    public List<Map<String, Object>> getAllBuses() throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/buses").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    public void createBus(String number, String name, String type, int seats, String amenities) throws IOException {
        Map<String, Object> payload = Map.of(
                "busNumber", number,
                "busName", name,
                "busType", type,
                "totalSeats", seats,
                "amenities", amenities,
                "active", true
        );
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/buses").post(body).build();
        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public void deleteBus(Long id) throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/buses/" + id).delete().build();
        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public List<Map<String, Object>> getAllRoutes() throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/routes").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    public void createRoute(String source, String dest, BigDecimal km, BigDecimal hours) throws IOException {
        Map<String, Object> payload = Map.of(
                "sourceCity", source,
                "destinationCity", dest,
                "distanceKm", km,
                "estimatedDurationHours", hours,
                "active", true
        );
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/routes").post(body).build();
        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public void deleteRoute(Long id) throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/routes/" + id).delete().build();
        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public List<Map<String, Object>> getAllSchedules() throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/schedules").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    public void createSchedule(Long busId, Long routeId, String deptTime, String arrTime, BigDecimal fare) throws IOException {
        Map<String, Object> payload = Map.of(
                "busId", busId,
                "routeId", routeId,
                "departureTime", deptTime,
                "arrivalTime", arrTime,
                "baseFare", fare,
                "status", "SCHEDULED"
        );
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.get("application/json"));
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/schedules").post(body).build();
        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public void deleteSchedule(Long id) throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/schedules/" + id).delete().build();
        try (Response response = httpClient.newCall(request).execute()) {
            ensureSuccess(response);
        }
    }

    public List<BookingModel> getAllBookings() throws IOException {
        Request request = newAuthenticatedRequest(baseUrl + "/api/admin/bookings").get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return extractListData(response, new TypeReference<List<BookingModel>>() {});
        }
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------
    private void ensureSuccess(Response response) throws IOException {
        String respStr = response.body() != null ? response.body().string() : "{}";
        JsonNode root = objectMapper.readTree(respStr);
        if (!response.isSuccessful() || !root.path("success").asBoolean(true)) {
            String msg = root.path("message").asText("Request failed (HTTP " + response.code() + ")");
            throw new IOException(msg);
        }
    }

    private <T> T extractObjectData(Response response, Class<T> clazz) throws IOException {
        String respStr = response.body() != null ? response.body().string() : "{}";
        JsonNode root = objectMapper.readTree(respStr);
        if (!response.isSuccessful() || !root.path("success").asBoolean(true)) {
            String msg = root.path("message").asText("Request failed");
            throw new IOException(msg);
        }
        return objectMapper.treeToValue(root.path("data"), clazz);
    }

    private <T> T extractObjectData(Response response, TypeReference<T> typeRef) throws IOException {
        String respStr = response.body() != null ? response.body().string() : "{}";
        JsonNode root = objectMapper.readTree(respStr);
        if (!response.isSuccessful() || !root.path("success").asBoolean(true)) {
            String msg = root.path("message").asText("Request failed");
            throw new IOException(msg);
        }
        return objectMapper.convertValue(root.path("data"), typeRef);
    }

    private <T> T extractListData(Response response, TypeReference<T> typeRef) throws IOException {
        String respStr = response.body() != null ? response.body().string() : "{}";
        JsonNode root = objectMapper.readTree(respStr);
        if (!response.isSuccessful() || !root.path("success").asBoolean(true)) {
            String msg = root.path("message").asText("Request failed");
            throw new IOException(msg);
        }
        return objectMapper.convertValue(root.path("data"), typeRef);
    }
}
