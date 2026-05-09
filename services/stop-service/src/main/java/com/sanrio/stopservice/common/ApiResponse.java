package com.sanrio.stopservice.common;

public record ApiResponse<T>(String message, T data) {
}
