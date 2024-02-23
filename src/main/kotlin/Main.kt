package org.example

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class MutablePair<A, B>(var l: A, var r: B)

@Serializable
data class Dish(val name: String, var price: Int = 0, var time: Duration = Duration.ZERO)

@Serializable
data class User(val login: String, val password: String)

suspend fun cook(dish: Dish) {
    delay(dish.time)
}

suspend fun main() = coroutineScope {
    val menuFile = File("data/menu.json")
    val usersFile = File("data/users.json")
    val revenueFile = File("data/revenue.json")
    val menu: MutableSet<MutablePair<Dish, Int>> =
        Json.decodeFromString(menuFile.readText(Charsets.UTF_8))
    val orders: MutableMap<User, MutableList<MutablePair<Dish, Job>>> = mutableMapOf()
    val users: MutableSet<User> = Json.decodeFromString(usersFile.readText(Charsets.UTF_8))
    var user: User? = null
    var revenue: Int = Json.decodeFromString(revenueFile.readText(Charsets.UTF_8))
    while (true) {
        try {
            val input = readlnOrNull()?.split(" ") ?: continue
            when (input[0]) {
                "auth" -> when (input[1]) {
                    "in" -> {
                        val inUser = User(input[2], input[3])
                        if (users.contains(inUser)) {
                            user = inUser
                            println("Success.")
                        } else {
                            println("There is no such user.")
                        }
                    }

                    "reg" -> users.add(User(input[2], input[3]))
                    else -> throw Exception("There is no such command. 'in' and 'reg' are available after 'auth'.")
                }

                "menu" -> when (input[1]) {
                    "add" -> {
                        if (user?.login != "admin") throw Exception("You need be admin to do this.")
                        menu.add(MutablePair(Dish(input[2]), 0))
                    }

                    "remove" -> {
                        if (user?.login != "admin") throw Exception("You need be admin to do this.")
                        menu.removeAll { it.l.name == input[2] }
                    }

                    "edit" -> {
                        if (user?.login != "admin") throw Exception("You need be admin to do this.")
                        when (input[2]) {
                            "price" -> menu.first { it.l.name == input[3] }.l.price = input[4].toInt()
                            "time" -> menu.first { it.l.name == input[3] }.l.time = input[4].toInt().seconds
                            "count" -> menu.first { it.l.name == input[3] }.r += input[4].toInt()
                            else -> throw Exception("There is no such command. 'price', 'time', 'count' are available after 'edit'.")
                        }
                    }

                    "list" -> menu.forEach { println("${it.l}, have ${it.r} ") }

                    else -> throw Exception("There is no such command. 'add', 'remove', 'edit' are available after 'menu'.")
                }

                "order" -> {
                    if (user == null) {
                        throw Exception("You must be logged for this.")
                    }
                    if (!orders.contains(user)) {
                        orders[user] = mutableListOf()
                    }
                    when (input[1]) {
                        "add" -> {
                            val menuItem = menu.firstOrNull { it.l.name == input[2] }
                                ?: throw Exception("There is no such dish in the menu.")
                            if (menuItem.r == 0) {
                                throw Exception("The dish is over.")
                            }
                            orders[user]!!.add(MutablePair(menuItem.l, launch { cook(menuItem.l) }))
                            menuItem.r -= 1
                        }

                        "remove" -> {
                            val menuItem = menu.firstOrNull { it.l.name == input[2] }
                                ?: throw Exception("There is no such dish in the menu.")
                            val orderItem = orders[user]!!.find { it.l == menuItem.l && it.r.isActive }
                                ?: throw Exception("There is no such not completed dish in your order.")
                            orderItem.r.cancel()
                            orders[user]!!.remove(orderItem)
                            menuItem.r += 1
                        }

                        "pay" -> {
                            val order = orders[user]!!
                            val completedItems = order.filter { it.r.isCompleted }
                            var orderSum = 0
                            completedItems.forEach { orderSum += it.l.price }
                            order.removeAll(completedItems)
                            revenue += orderSum
                            println("Paid in $orderSum.")
                        }

                        "list" -> {
                            val order = orders[user]!!
                            var sumToPay = 0
                            order.forEach {
                                println(
                                    "${it.l}, status: ${
                                        if (it.r.isCompleted) {
                                            "ready"
                                        } else {
                                            "accepted"
                                        }
                                    }"
                                )
                                sumToPay += it.l.price
                            }
                            println("Total sum to pay: $sumToPay")
                        }

                        else -> throw Exception("There is no such command. 'add', 'remove', 'list', 'pay' are available after 'order'.")
                    }
                }

                "revenue" -> println(revenue)

                "q" -> {
                    menuFile.writeText(Json.encodeToString(menu))
                    usersFile.writeText(Json.encodeToString(users))
                    revenueFile.writeText(Json.encodeToString(revenue))
                    println("Exit")
                    break
                }

                else -> throw Exception("There is no such command.")
            }
        } catch (e: Exception) {
            println("Invalid operation, try again, may be useful: ${e.message}")
        }
    }
}