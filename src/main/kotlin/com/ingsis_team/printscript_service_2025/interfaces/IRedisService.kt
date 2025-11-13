package com.ingsis_team.printscript_service_2025.interfaces

import com.ingsis_team.printscript_service_2025.redis.dto.Snippet

interface IRedisService {
    fun formatSnippet(snippet: Snippet): Snippet

    fun lintSnippet(snippet: Snippet): Snippet

    fun testSnippet(snippet: Snippet): Snippet
}
