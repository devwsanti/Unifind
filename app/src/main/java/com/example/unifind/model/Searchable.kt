package com.example.unifind.model

/**
 * OOP Concept: Interface / Abstraction
 * Defines a contract for objects that can be searched using a query string.
 */
interface Searchable {
    fun matches(query: String): Boolean
    // matches - means ang item ba na sinearch ng user is nag mamatch sa sinearch nya
    // query : string - the input parameter
    // query means the exact text typed by the user in the search bar (e.g "lian cafeteria" or "room101")
}
