package com.simplemobiletools.gallery.pro.models

class RemoteServer(
    val id: Long?,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val passwordHash: String, // encrypted or hashed
    val remotePath: String
)
