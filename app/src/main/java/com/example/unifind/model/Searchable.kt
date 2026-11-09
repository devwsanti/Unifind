package com.example.unifind.model

/**
 * OOP Concept: Interface / Abstraction
 * Defines a contract for objects that can be searched using a query string.
 */
interface Searchable {
    fun matches(query: String): Boolean
}
