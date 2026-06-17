package com.financecontrol.enums;

/**
 * Ciclo de vida de uma dica do Finny.
 * NEW       — gerada, ainda não entregue (reservado para uso futuro / geração em lote)
 * SHOWN     — entregue ao usuário, aguardando feedback
 * HELPFUL   — usuário marcou como útil (👍)
 * NOT_HELPFUL — usuário marcou como não útil (👎)
 * DISMISSED — usuário dispensou a dica
 */
public enum FinnyTipStatus {
    NEW,
    SHOWN,
    HELPFUL,
    NOT_HELPFUL,
    DISMISSED
}
