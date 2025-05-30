package tech.yump.veriboard.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.yump.veriboard.clients.notification.NotificationRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendNotification_ShouldCallNotificationService() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest(
                1,
                "test@example.com",
                "Test notification"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService).send(any(NotificationRequest.class));
    }

    @Test
    void sendNotification_WithNullFields_ShouldStillCallService() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest(
                null,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService).send(any(NotificationRequest.class));
    }

    @Test
    void sendNotification_WithValidData_ShouldReturn200() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest(
                123,
                "customer@example.com",
                "Welcome to VeriBoard!"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService).send(request);
    }
} 