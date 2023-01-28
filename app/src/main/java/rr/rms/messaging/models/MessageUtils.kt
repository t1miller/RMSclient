package rr.rms.messaging.models

import java.util.*
import kotlin.random.Random

object MessageUtils {

    private fun randomGreeting() : String {
        val greetings = listOf(
            "Hello",
            "Woof",
            "Yo",
            "Hey",
            "M'Lady",
            "Good Morning",
            "Good Afternoon",
            "Woof Woof",
            "Woof Woof Woof"
        )
        val randIndex = Random.nextInt(0, greetings.size)
        return greetings[randIndex]
    }

    private fun randomName() : String {
        val names = listOf(
            "lamb",
            "clyde",
            "kate",
            "trent",
            "eva",
            "steve",
            "nick",
            "savannah"
        )
        val randIndex = Random.nextInt(0, names.size)
        return names[randIndex]
    }

    private fun randomUser() : String {
        return "User:trent"
    }

    private fun randomId(): String {
        return UUID.randomUUID().toString()
    }

    fun randomMessage(): Message {
        return Message(
            randomGreeting(),
            randomUser()
        )
    }
}