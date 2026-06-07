package com.example.invitevip.customer.event;

// 검색 인덱스 동기화가 필요하다는 이벤트
public record CustomerSearchSyncEvent(Long customerId, CustomerSearchSyncType type) {

    public static CustomerSearchSyncEvent upsert(Long customerId) {
        return new CustomerSearchSyncEvent(customerId, CustomerSearchSyncType.UPSERT);
    }

    public static CustomerSearchSyncEvent delete(Long customerId) {
        return new CustomerSearchSyncEvent(customerId, CustomerSearchSyncType.DELETE);
    }
}
