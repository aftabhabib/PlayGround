package com.desimeals.app.data.local

/**
 * Unified ledger entry. Works fully offline in Room.
 *
 * type: CHARGE (mess bill added), PAYMENT (cash/UPI received from member),
 *       EXPENSE (mess running cost: groceries, gas, salary),
 *       INCOME (other income)
 * customerId: null for mess-level EXPENSE/INCOME not tied to a member.
 */
enum class TransactionType { CHARGE, PAYMENT, EXPENSE, INCOME }

enum class MealType { BREAKFAST, LUNCH, DINNER, MONTHLY_BILL, OTHER }

@androidx.room.Entity(
    tableName = "transactions",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("customerId"), androidx.room.Index("date")]
)
data class TransactionEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val type: TransactionType = TransactionType.CHARGE,
    val amount: Double = 0.0,
    val description: String = "",
    val mealType: MealType = MealType.OTHER,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
