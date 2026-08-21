package com.bookworm.payment;

import com.bookworm.api.model.PaymentMethod;
import com.bookworm.api.model.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Deterministic mock of a card / UPI / wallet gateway.
 *
 * Decline rules (plan §1):
 * <ul>
 *   <li>CREDIT or DEBIT with cardNumber ending in {@code 0000} → DECLINED
 *   <li>UPI with upiId starting with {@code fail@} → DECLINED
 *   <li>everything else → SUCCESS
 * </ul>
 *
 * The 1.5s "processing" spinner is a frontend concern (per plan §1),
 * so this method returns synchronously without sleeping.
 */
@Service
@Slf4j
public class MockPaymentGateway {

    private static final String FAIL_CARD_SUFFIX = "0000";
    private static final String FAIL_UPI_PREFIX  = "fail@";

    public record ChargeResult(boolean success, String transactionRef, String declineReason) {}

    public ChargeResult charge(PaymentRequest req, int amountPaise) {
        PaymentMethod method = req.getMethod();
        String txn = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        boolean declined = switch (method) {
            case CREDIT, DEBIT -> req.getCardNumber() != null
                                   && stripSpaces(req.getCardNumber()).endsWith(FAIL_CARD_SUFFIX);
            case UPI           -> req.getUpiId() != null
                                   && req.getUpiId().toLowerCase().startsWith(FAIL_UPI_PREFIX);
            case WALLET        -> false;
        };

        if (declined) {
            log.info("MOCK PAYMENT DECLINED  method={} amount={}paise txn={}", method, amountPaise, txn);
            return new ChargeResult(false, txn, declineReason(method));
        }
        log.info("MOCK PAYMENT SUCCESS   method={} amount={}paise txn={}", method, amountPaise, txn);
        return new ChargeResult(true, txn, null);
    }

    private static String stripSpaces(String s) {
        return s == null ? "" : s.replace(" ", "").replace("-", "");
    }

    private static String declineReason(PaymentMethod method) {
        return switch (method) {
            case CREDIT, DEBIT -> "Card declined by issuing bank";
            case UPI           -> "UPI ID rejected by PSP";
            case WALLET        -> "Wallet balance insufficient";
        };
    }
}
