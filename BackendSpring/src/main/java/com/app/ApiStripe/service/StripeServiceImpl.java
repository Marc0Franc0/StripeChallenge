package com.app.ApiStripe.service;

import com.app.ApiStripe.dto.PaymentIntentDTO;
import com.app.dto.ResponseSub;
import com.app.model.Payment;
import com.app.model.Subscription;
import com.app.security.model.UserEntity;
import com.app.security.service.UserEntityService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class StripeServiceImpl implements StripeService{
    @Value("${api.stripe.key.private}")
   private String stripeApiKey;
    @Autowired
    UserEntityService userEntityService;
    //Método para crear pago
    public PaymentIntent createPaymenIntent(PaymentIntentDTO paymentIntentDTO){
        Stripe.apiKey = stripeApiKey;
        try {
            //Se crea el pago
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            //Monto
                            .setAmount(paymentIntentDTO.getAmount())
                            //Moneda
                            .setCurrency(paymentIntentDTO.getCurrency())
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                            .setEnabled(true)
                                            .build())
                            .build();
            //Se retorna el pago creado
            return PaymentIntent.create(params);
        }catch (Exception e){
            throw new RuntimeException
                    ("No se pudo crear el pago de suscripción: ".concat(e.getMessage()));
        }
    }
    //Método para confirmar pago
    public PaymentIntent confirmPayment(ResponseSub responseSub) {
        Stripe.apiKey = stripeApiKey;
        //Se obtiene el pago con su id
        PaymentIntent resource = null;
        try {
            resource = PaymentIntent.retrieve(responseSub.getStripeId());

        //Se crea la confirmación del pago
        PaymentIntentConfirmParams params =
                PaymentIntentConfirmParams.builder()
                        .setPaymentMethod("pm_card_visa")
                        .setReturnUrl("https://www.example.com")
                        .build();
        //Se obtiene el usuario
        Optional<UserEntity> user = userEntityService.getUser(responseSub.getUsername());
            PaymentIntent paymentIntent = null;
                paymentIntent = resource.confirm(params);
            //Se actualiza la subscripción de usuario
            Subscription subSaved = user.get().getSubscription();
            Subscription sub =
                    Subscription
                            .builder()
                            .id(subSaved.getId())
                            .active(true)
                            .startDate(LocalDate.now())
                            .subscriptionType(subSaved.getSubscriptionType())
                            .endDate(LocalDate.now().plusDays(subSaved.getSubscriptionType().getDurationDays()))
                            .payment(Payment.builder()
                                    .id(user.get().getSubscription().getPayment().getId())
                                    .idStripe(paymentIntent.getId())
                                    .status(paymentIntent.getStatus())
                                    .user(user.get()).build())
                            .build();
            userEntityService.updateUserSub(user.get(),sub);
            return resource;
        }catch(Exception e){
            throw new RuntimeException
                    ("No se pudo confirmar el pago de suscripción: ".concat(e.getMessage()));
            }

    }
    //Método para cancelar pago
    public PaymentIntent cancelPayment(String id){
        Stripe.apiKey = stripeApiKey;
        //Se obtiene el pago con su id

        PaymentIntent resource = null;
        try {
            resource = PaymentIntent.retrieve(id);
        //Se crea la cancelación del pago
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
        //Se retorna la cancelación
            return resource.cancel(params);
        }catch (StripeException e) {
            throw new RuntimeException
                    ("No se pudo cancelar el pago de suscripción:".concat(e.getMessage()));
        }
    }
}
