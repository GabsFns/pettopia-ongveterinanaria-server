package com.example.OngVeterinaria.controller;

import com.example.OngVeterinaria.model.DinheiroModel;
import com.example.OngVeterinaria.services.DinheiroServices;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/doacoes")
public class DinheiroController {

    @Value("${mercado_pago.access_token}")
    private String mercadoPagoAccessToken;

    @Autowired
    private DinheiroServices dinheiroService;

    @PostMapping("/criar-preferencia")
    public Map<String, String> criarPreferencia(@RequestBody Map<String, Object> request) throws MPException {
        Double valor = Double.parseDouble(request.get("valor").toString());
        Long clienteId = request.containsKey("clienteId") ? Long.parseLong(request.get("clienteId").toString()) : null;

        // Configura o Mercado Pago com o access_token
        MercadoPago.SDK.setAccessToken(mercadoPagoAccessToken);

        // Cria a preferência
        Preference preference = new Preference();

        // Cria um item para a doação
        Item item = new Item();
        item.setTitle("Doação para ONG")
                .setQuantity(1)
                .setUnitPrice(valor.floatValue());

        // Adiciona o item à preferência
        preference.appendItem(item);

        // Configura a URL de redirecionamento após o pagamento
        BackUrls backUrls = new BackUrls();
        backUrls.setSuccess("http://localhost:8081/doacoes/success?valor=" + valor);
        backUrls.setFailure("http://localhost:8081/doacoes/failure?status=failure");
        preference.setBackUrls(backUrls);

        // Salva a preferência no Mercado Pago
        preference.save();

        // Se o cliente estiver logado, associa a doação ao cliente
        if (clienteId != null) {
            // Aqui você pode buscar o cliente pelo ID e associar a doação a ele, se necessário
            dinheiroService.salvarDoacaoComCliente(valor, clienteId);
        } else {
            // Se o cliente não estiver logado, trata a doação como anônima
            dinheiroService.salvarDoacao(valor);
        }

        // Retorna o ID da preferência para o front-end
        Map<String, String> response = new HashMap<>();
        response.put("preferenceId", preference.getId());
        return response;
    }


    //NOTIFICAO PARA PROCESSAMENTO DE GRAVCACAO NO BANCO, EM PROCESSO

//    @PostMapping("/webhook")
//    public void handlePaymentNotification(@RequestBody String jsonNotification) throws MPException {
//        // Configura o Mercado Pago com o access_token para verificar o pagamento
//        MercadoPago.SDK.setAccessToken(mercadoPagoAccessToken);
//
//        // Parseia a notificação para obter o id do pagamento
//        Payment payment = Payment.findById(jsonNotification);
//
//        // Verifica o status do pagamento
//        if (payment != null && "approved".equals(payment.getStatus())) {
//            // Salva a doação no banco apenas quando o pagamento for aprovado
//            Double valor = Double.valueOf(payment.getTransactionAmount());
//            dinheiroService.salvarDoacao(valor);
//        } else {
//            // Lidar com o caso em que o pagamento não foi aprovado, se necessário
//            System.out.println("Pagamento não aprovado ou não encontrado.");
//        }
//    }

    @GetMapping("/success")
    public String successPayment() {
        return "Pagamento realizado com sucesso!";
    }

    @GetMapping("/failure")
    public String failurePayment() {
        return "Falha no pagamento.";
    }


}