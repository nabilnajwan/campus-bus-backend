package com.sanrio.authservice.common;

public record ApiResponse<T>(String message, T data) {
}
