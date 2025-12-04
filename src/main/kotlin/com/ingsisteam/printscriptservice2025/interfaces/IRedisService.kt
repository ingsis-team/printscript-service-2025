package com.ingsisteam.printscriptservice2025.interfaces

import com.ingsisteam.printscriptservice2025.redis.dto.Snippet

interface IRedisService {
    fun formatSnippet(snippet: Snippet): Snippet

    fun lintSnippet(snippet: Snippet): Snippet

    fun testSnippet(snippet: Snippet): Snippet
}
