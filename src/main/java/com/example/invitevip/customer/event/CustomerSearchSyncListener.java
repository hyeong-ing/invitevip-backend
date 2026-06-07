package com.example.invitevip.customer.event;

import com.example.invitevip.customer.database.CustomerRepository;
import com.example.invitevip.customer.database.CustomerSearchRepository;
import com.example.invitevip.customer.mapper.CustomerSearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// DB 커밋 이후 Elasticsearch를 실제로 갱신하는 클래스
@Component
@RequiredArgsConstructor
public class CustomerSearchSyncListener {

    private final CustomerRepository customerRepository;
    private final CustomerSearchRepository customerSearchRepository;
    private final CustomerSearchMapper customerSearchMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sync(CustomerSearchSyncEvent event) {
        if (event.type() == CustomerSearchSyncType.DELETE) {
            customerSearchRepository.deleteById(event.customerId());
            return;
        }

        customerRepository.findById(event.customerId())
                .map(customerSearchMapper::toSearchEntity)
                .ifPresent(customerSearchRepository::save);
    }
}
