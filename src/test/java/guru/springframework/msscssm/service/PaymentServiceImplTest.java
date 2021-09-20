package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void testDoPreAuth() {
        //NEW Payment
        Payment savePayment=paymentService.newPayment(payment);

        System.out.println("Should be New");
        System.out.println(savePayment.getState());

        //PRE AUTH
        StateMachine<PaymentState, PaymentEvent> sm =  paymentService.preAuth(savePayment.getId());
        Payment preAuthedPayment =  paymentRepository.getById(savePayment.getId());

        System.out.println("Should be PRE_AUTH or PRE_AUTH_ERROR");
        System.out.println(sm.getState().getId());
        System.out.println(preAuthedPayment);
    }

    @Transactional
    @RepeatedTest(10)
    void testDoAuth() {
        //NEW Payment
        Payment savePayment=paymentService.newPayment(payment);

        StateMachine<PaymentState, PaymentEvent> preAuthSM = paymentService.preAuth(savePayment.getId());

        if(preAuthSM.getState().getId() == PaymentState.PRE_AUTH){
            System.out.println("Payment is Pre Authorized");
            StateMachine<PaymentState, PaymentEvent> authSM = paymentService.authorizePayment(savePayment.getId());

            System.out.println("Result of Auth: "+ authSM.getState().getId());
        }else {
            System.out.println("Payment falid pre-auth...");
        }
    }
}