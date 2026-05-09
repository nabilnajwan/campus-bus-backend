package com.sanrio.tripservice.common;

public record ApiResponse<T>(String message, T data) {
}
