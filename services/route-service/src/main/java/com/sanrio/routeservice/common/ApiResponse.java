package com.sanrio.routeservice.common;

public record ApiResponse<T>(String message, T data) {
}
