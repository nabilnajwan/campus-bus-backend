package com.sanrio.busservice.common;

public record ApiResponse<T>(String message, T data) {
}
