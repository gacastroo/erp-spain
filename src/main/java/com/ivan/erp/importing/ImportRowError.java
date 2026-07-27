package com.ivan.erp.importing;

public record ImportRowError(int rowNumber, String identifier, String message) {
}
