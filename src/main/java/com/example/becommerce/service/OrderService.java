package com.example.becommerce.service;

import com.example.becommerce.dto.request.order.CancelOrderRequest;
import com.example.becommerce.dto.request.order.CompleteOrderRequest;
import com.example.becommerce.dto.request.order.CreateOrderRequest;
import com.example.becommerce.dto.request.order.PriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.RejectOrderRequest;
import com.example.becommerce.dto.request.order.RejectPriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.SelectPaymentMethodRequest;
import com.example.becommerce.dto.request.order.UpdateOrderStatusRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.order.OrderPaymentResponse;
import com.example.becommerce.dto.response.order.OrderResponse;
import com.example.becommerce.dto.response.order.OrderStatusChangeResponse;
import com.example.becommerce.dto.response.order.PriceAdjustmentEnvelope;

/**
 * Business operations on Orders. Visibility & authorization checks
 * are enforced inside the service layer based on the current principal.
 */
public interface OrderService {

    // ---- Listing / retrieval ---------------------------------------

    PagedResponse<OrderResponse> getOrders(String status, String keyword, int page, int limit);

    OrderResponse getOrderById(String code);

    // ---- Customer flows --------------------------------------------

    OrderResponse createOrder(CreateOrderRequest request);

    OrderStatusChangeResponse cancelOrder(String code, CancelOrderRequest request);

    PriceAdjustmentEnvelope approvePriceAdjustment(String code);

    PriceAdjustmentEnvelope rejectPriceAdjustment(String code, RejectPriceAdjustmentRequest request);

    // ---- Technician flows ------------------------------------------

    OrderStatusChangeResponse acceptOrder(String code);

    OrderStatusChangeResponse rejectOrder(String code, RejectOrderRequest request);

    OrderStatusChangeResponse updateStatus(String code, UpdateOrderStatusRequest request);

    OrderStatusChangeResponse completeOrder(String code, CompleteOrderRequest request);

    PriceAdjustmentEnvelope requestPriceAdjustment(String code, PriceAdjustmentRequest request);

    // ---- Payment flow --------------------------------------------------

    /**
     * Customer picks a payment method (cash or VNPay) for an order that is
     * awaiting payment. Cash settles immediately; VNPay returns a checkout URL
     * and the order is finalized later via {@link #completeOrderAfterPayment(Long)}.
     */
    OrderPaymentResponse selectPaymentMethod(String code, SelectPaymentMethodRequest request);

    /**
     * Technician confirms they have physically received the cash from the customer.
     * Deducts the order's commission from the technician's credit wallet (crediting
     * the admin wallet) and finalizes the order. (Task-28)
     */
    OrderStatusChangeResponse confirmCashPayment(String code);

    /**
     * Finalize an order once its payment has been confirmed (e.g. from the VNPay
     * IPN callback): move it to COMPLETED and run the commission split. Idempotent.
     */
    void completeOrderAfterPayment(Long orderId);
}
