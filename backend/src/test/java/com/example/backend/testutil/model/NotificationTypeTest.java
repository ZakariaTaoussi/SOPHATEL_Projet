package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.enums.NotificationType;
import org.junit.jupiter.api.Test;

class NotificationTypeTest {

    @Test
    void shouldExposeExpectedValues() {
        assertThat(NotificationType.values())
                .containsExactly(
                        NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE,
                        NotificationType.DEMANDE_MODIFIED_BY_EMPLOYE,
                        NotificationType.DEMANDE_CANCELLED_BY_EMPLOYE,
                        NotificationType.DEMANDE_VALIDATED_BY_RESPONSABLE,
                        NotificationType.DEMANDE_MODIFIED_BY_RESPONSABLE,
                        NotificationType.DEMANDE_REFUSED_BY_RESPONSABLE,
                        NotificationType.DEMANDE_VALIDATED_BY_DG,
                        NotificationType.DEMANDE_REFUSED_BY_DG,
                        NotificationType.DEMANDE_VALIDATED_DG_FOR_RH);
    }
}
