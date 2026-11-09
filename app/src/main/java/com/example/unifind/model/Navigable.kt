package com.example.unifind.model

/**
 * OOP Concept: Interface / Abstraction
 * Defines a contract for objects that provide an entry point for navigation.
 */
interface Navigable {
    fun getEntryPoint(): Pair<Float, Float>
}
