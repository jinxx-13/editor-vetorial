package org.example.Controller;

/** Arquivo de desenho corrompido ou em formato inválido. */
public class ArquivoInvalidoException extends Exception {
    public ArquivoInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
