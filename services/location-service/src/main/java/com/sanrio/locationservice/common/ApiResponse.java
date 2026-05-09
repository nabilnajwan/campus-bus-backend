package com.sanrio.locationservice.common;

public record ApiResponse<T>(String message, T data) {
}
