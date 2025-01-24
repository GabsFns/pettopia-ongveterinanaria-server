package com.example.OngVeterinaria.model;

public class PedidoAdocaoRequest {
    public PedidoModel getPedido() {
        return pedido;
    }

    public void setPedido(PedidoModel pedido) {
        this.pedido = pedido;
    }

    public byte[] getComprovanteRenda() {
        return comprovanteRenda;
    }

    public void setComprovanteRenda(byte[] comprovanteRenda) {
        this.comprovanteRenda = comprovanteRenda;
    }

    private PedidoModel pedido;
    private byte[] comprovanteRenda;
}
