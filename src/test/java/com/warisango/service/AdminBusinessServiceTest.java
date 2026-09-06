package com.warisango.service;

import com.warisango.exception.OperationConflictException;
import com.warisango.model.HeritageBusiness;
import com.warisango.repository.AdminRepository;
import com.warisango.repository.BusinessRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminBusinessServiceTest {
    private final BusinessRepository repository = mock(BusinessRepository.class);
    private final AdminRepository admins = mock(AdminRepository.class);
    private final AdminBusinessService service = new AdminBusinessService(
            repository, mock(BusinessPhotoService.class), admins);

    private void givenStatus(String status) {
        when(admins.existsByUserId("admin")).thenReturn(true);
        when(repository.findHeritageBusinessById("business")).thenReturn(Optional.of(new HeritageBusiness(
                "business", "Name", null, null, null, null, null, null, null, null,
                status, null, 50, null, null, null)));
    }

    @Test
    void rejectedBusinessesCannotBeActivatedOrDeactivated() {
        givenStatus("Rejected");
        assertThrows(OperationConflictException.class, () -> service.setActive("business", true, "admin"));
        assertThrows(OperationConflictException.class, () -> service.setActive("business", false, "admin"));
        verify(repository, never()).approve(anyString());
        verify(repository, never()).deactivate(anyString());
    }

    @Test
    void pendingBusinessesCannotBypassReview() {
        givenStatus("Pending");
        assertThrows(OperationConflictException.class, () -> service.setActive("business", true, "admin"));
        assertThrows(OperationConflictException.class, () -> service.setActive("business", false, "admin"));
        verify(repository, never()).approve(anyString());
        verify(repository, never()).deactivate(anyString());
    }

    @Test
    void approvedBusinessCanBeDeactivated() {
        givenStatus("Approved");
        service.setActive("business", false, "admin");
        verify(repository).deactivate("business");
    }

    @Test
    void inactiveBusinessCanBeReactivated() {
        givenStatus("Inactive");
        service.setActive("business", true, "admin");
        verify(repository).approve("business");
    }

    @Test
    void repeatedActivationDoesNotRewriteApproval() {
        givenStatus("Approved");
        service.setActive("business", true, "admin");
        verify(repository, never()).approve(anyString());
    }
}
