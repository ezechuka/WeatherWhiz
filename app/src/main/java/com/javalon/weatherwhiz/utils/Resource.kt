package com.javalon.weatherwhiz.utils

class Resource<out T>(val status: Status, val data: T?, val msg: String?) {
    companion object {
        fun <T> success(data: T?) : Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(data: T?, msg: String?) : Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?) : Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}