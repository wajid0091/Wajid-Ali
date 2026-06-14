package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject
import org.json.JSONArray
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

sealed interface Screen {
    object Login : Screen
    object Register : Screen
    object Dashboard : Screen // Bottom Nav Tabs: Home, Games, Store, Rewards, Profile
    data class TournamentDetails(val tournamentId: Int) : Screen
    data class EditProfile(val userId: Int) : Screen
    object ReferAndEarn : Screen
    object MatchHistory : Screen
    object WalletScreen : Screen
    object DepositScreen : Screen
    object WithdrawScreen : Screen
    object AdminLogin : Screen
    object AdminDashboard : Screen
    object AdminUserManagement : Screen
    object AdminTournamentManagement : Screen
    object AdminBannerManagement : Screen
    object AdminDepositManagement : Screen
    object AdminWithdrawManagement : Screen
    object AdminRewardManagement : Screen
    object AdminNotificationManagement : Screen
    object AdminSettingsManagement : Screen
}

enum class HomeTab {
    Home, Games, Store, Rewards, Profile
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val sharedPrefs = application.getSharedPreferences("anu_battle_prefs", Context.MODE_PRIVATE)

    // --- Navigation Backstack State ---
    private val _navigationStack = MutableStateFlow<List<Screen>>(listOf(Screen.Login))
    val navigationStack: StateFlow<List<Screen>> = _navigationStack.asStateFlow()

    // --- Active Tab State ---
    private val _currentTab = MutableStateFlow(HomeTab.Home)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    // --- Theme State ---
    private val _isDarkMode = MutableStateFlow(true) // default dark mode for esports feel
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // --- Current Logged In User State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // --- Status Messages ---
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // --- Lists (Exposed from Room Flows) ---
    val tournaments: StateFlow<List<Tournament>> = MutableStateFlow(emptyList())
    val banners: StateFlow<List<BannerBanner>> = MutableStateFlow(emptyList())
    val coinPackages: StateFlow<List<CoinPackage>> = MutableStateFlow(emptyList())
    val notifications: StateFlow<List<Notification>> = MutableStateFlow(emptyList())
    
    // User-specific states
    private val _userJoinedTournaments = MutableStateFlow<List<JoinedTournament>>(emptyList())
    val userJoinedTournaments: StateFlow<List<JoinedTournament>> = _userJoinedTournaments.asStateFlow()

    private val _userTransactions = MutableStateFlow<List<TransactionHistory>>(emptyList())
    val userTransactions: StateFlow<List<TransactionHistory>> = _userTransactions.asStateFlow()

    private val _userDeposits = MutableStateFlow<List<DepositRequest>>(emptyList())
    val userDeposits: StateFlow<List<DepositRequest>> = _userDeposits.asStateFlow()

    private val _userWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val userWithdrawals: StateFlow<List<WithdrawalRequest>> = _userWithdrawals.asStateFlow()

    private val _userDailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val userDailyTasks: StateFlow<List<DailyTask>> = _userDailyTasks.asStateFlow()

    // Admin Specific Lists
    val allUsers: StateFlow<List<User>> = MutableStateFlow(emptyList())
    val allDeposits: StateFlow<List<DepositRequest>> = MutableStateFlow(emptyList())
    val allWithdrawals: StateFlow<List<WithdrawalRequest>> = MutableStateFlow(emptyList())
    val allSettings: StateFlow<List<AppSetting>> = MutableStateFlow(emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Sync lists with Flows
        viewModelScope.launch {
            repository.allSettings.collect { (allSettings as MutableStateFlow).value = it }
        }
        viewModelScope.launch {
            repository.allTournaments.collect { (tournaments as MutableStateFlow).value = it }
        }
        viewModelScope.launch {
            repository.allBanners.collect { (banners as MutableStateFlow).value = it }
        }
        viewModelScope.launch {
            repository.allCoinPackages.collect { (coinPackages as MutableStateFlow).value = it }
        }
        viewModelScope.launch {
            repository.allUsers.collect { (allUsers as MutableStateFlow).value = it }
        }
        viewModelScope.launch {
            repository.allDeposits.collect { (allDeposits as MutableStateFlow).value = it }
        }
        viewModelScope.launch {
            repository.allWithdrawals.collect { (allWithdrawals as MutableStateFlow).value = it }
        }

        // Dark theme state
        _isDarkMode.value = sharedPrefs.getBoolean("dark_mode", true)

        // Seed initial database
        seedDatabaseIfNeeded()

        // Auto login session setup
        checkAutoLogin()

        // Sync and pull live tournaments, banners, and settings from Firebase RTDB
        syncFromFirebase()
    }

    fun updateSetting(key: String, value: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertSetting(AppSetting(key, value))
                
                val client = OkHttpClient()
                val escapedValue = JSONObject.quote(value)
                val body = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    escapedValue
                )
                val request = Request.Builder()
                    .url("https://free-fire-tour-bee66-default-rtdb.firebaseio.com/settings/$key.json")
                    .put(body)
                    .build()
                
                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onComplete?.invoke(success)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            }
        }
    }

    // --- NAVIGATION API ---
    fun navigateTo(screen: Screen) {
        val current = _navigationStack.value.toMutableList()
        current.add(screen)
        _navigationStack.value = current
    }

    fun navigateBack() {
        val current = _navigationStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _navigationStack.value = current
        }
    }

    fun navigateToTab(tab: HomeTab) {
        _currentTab.value = tab
        // Clean navigation stack so it keeps Dashboard at root
        _navigationStack.value = listOf(Screen.Dashboard)
    }

    fun toggleTheme() {
        val nextVal = !_isDarkMode.value
        _isDarkMode.value = nextVal
        sharedPrefs.edit().putBoolean("dark_mode", nextVal).apply()
    }

    // --- TOAST TRIGGER ---
    fun triggerToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // --- AUTHENTICATION API ---
    private fun checkAutoLogin() {
        val savedUserId = sharedPrefs.getInt("logged_in_user_id", -1)
        if (savedUserId != -1) {
            viewModelScope.launch(Dispatchers.IO) {
                val user = repository.getUserById(savedUserId)
                if (user != null && !user.isBanned) {
                    _currentUser.value = user
                    loadUserData(user.id)
                    _navigationStack.value = listOf(Screen.Dashboard)
                } else {
                    // clear stale state
                    sharedPrefs.edit().remove("logged_in_user_id").apply()
                }
            }
        }
    }

    fun registerUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getUserByEmail(user.email)
            if (existing != null) {
                triggerToast("Email already registered!")
                onResult(false)
                return@launch
            }

            // Insert new user
            val userId = repository.insertUser(user)
            if (userId > 0) {
                val registeredUser = repository.getUserById(userId.toInt())
                if (registeredUser != null) {
                    // Initialize default Daily Tasks for user
                    initializeDailyTasks(userId.toInt())
                    
                    // Create dynamic greeting notification
                    repository.insertNotification(Notification(
                        userId = userId.toInt(),
                        title = "Welcome to Anu Battle!",
                        message = "Congratulations! You have received a 100 Main and 10 Bonus wallet starter credit.",
                        date = getCurrentDateString(),
                        time = getCurrentTimeString(),
                        type = "Announcement"
                    ))

                    // If referred, credit bonus reward
                    if (!user.referredBy.isNullOrEmpty()) {
                        val referrer = repository.getUserByReferralCode(user.referredBy)
                        if (referrer != null) {
                            // Credit referrer 15.0 Main Wallet + load
                            val updatedReferrer = referrer.copy(
                                mainWallet = referrer.mainWallet + 15.0,
                                referralCount = referrer.referralCount + 1,
                                coins = referrer.coins + 20
                            )
                            repository.updateUser(updatedReferrer)
                            repository.insertTransactionHistory(TransactionHistory(
                                userId = referrer.id,
                                amount = 15.0,
                                type = "Referral Bonus",
                                walletType = "Main Wallet",
                                status = "Completed",
                                description = "Referral code used by ${user.username}",
                                date = getCurrentDateString(),
                                time = getCurrentTimeString()
                            ))
                            repository.insertNotification(Notification(
                                userId = referrer.id,
                                title = "Referral Reward Credited",
                                message = "You received 15.0 PKR and 20 Coins because ${user.username} joined using your code.",
                                date = getCurrentDateString(),
                                time = getCurrentTimeString(),
                                type = "Rewards Available"
                            ))
                        }
                    }

                    _currentUser.value = registeredUser
                    sharedPrefs.edit().putInt("logged_in_user_id", registeredUser.id).apply()
                    loadUserData(registeredUser.id)
                    _navigationStack.value = listOf(Screen.Dashboard)
                    triggerToast("Registration Successful!")
                    onResult(true)
                }
            } else {
                triggerToast("Error in registration! Try again.")
                onResult(false)
            }
        }
    }

    fun loginUser(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                triggerToast("Account does not exist!")
                return@launch
            }
            if (user.password != password) {
                triggerToast("Incorrect password. Please try again.")
                return@launch
            }
            if (user.isBanned) {
                triggerToast("Your account is banned. Contact support.")
                return@launch
            }

            _currentUser.value = user
            if (rememberMe) {
                sharedPrefs.edit().putInt("logged_in_user_id", user.id).apply()
            } else {
                sharedPrefs.edit().remove("logged_in_user_id").apply()
            }
            loadUserData(user.id)
            _navigationStack.value = listOf(Screen.Dashboard)
            triggerToast("Logged in as ${user.username}")
        }
    }

    fun logout() {
        sharedPrefs.edit().remove("logged_in_user_id").apply()
        _currentUser.value = null
        _navigationStack.value = listOf(Screen.Login)
        _currentTab.value = HomeTab.Home
        triggerToast("Logged out successfully!")
    }

    // --- LOAD USER SPECIFIC TABLES ---
    private fun loadUserData(userId: Int) {
        viewModelScope.launch {
            repository.getUserByIdFlow(userId).collect {
                _currentUser.value = it
            }
        }
        viewModelScope.launch {
            repository.getJoinedTournamentsForUser(userId).collect {
                _userJoinedTournaments.value = it
            }
        }
        viewModelScope.launch {
            repository.getTransactionsForUser(userId).collect {
                _userTransactions.value = it
            }
        }
        viewModelScope.launch {
            repository.getDepositsForUser(userId).collect {
                _userDeposits.value = it
            }
        }
        viewModelScope.launch {
            repository.getWithdrawalsForUser(userId).collect {
                _userWithdrawals.value = it
            }
        }
        viewModelScope.launch {
            repository.getNotificationsForUser(userId).collect {
                (notifications as MutableStateFlow).value = it
            }
        }
        viewModelScope.launch {
            repository.getTasksForUserFlow(userId).collect {
                _userDailyTasks.value = it
            }
        }
    }

    private suspend fun initializeDailyTasks(userId: Int) {
        val tasks = listOf(
            DailyTask(userId = userId, taskType = "PLAY_2", title = "Play Game 2 Minutes", targetCount = 2, currentCount = 0, rewardCoins = 5),
            DailyTask(userId = userId, taskType = "PLAY_15", title = "Play Game 15 Minutes", targetCount = 15, currentCount = 0, rewardCoins = 10),
            DailyTask(userId = userId, taskType = "PLAY_20", title = "Play Game 20 Minutes", targetCount = 20, currentCount = 0, rewardCoins = 15),
            DailyTask(userId = userId, taskType = "JOIN_TOURNAMENT", title = "Join 1 Tournament", targetCount = 1, currentCount = 0, rewardCoins = 8),
            DailyTask(userId = userId, taskType = "WIN_TOURNAMENT", title = "Win 1 Tournament Match", targetCount = 1, currentCount = 0, rewardCoins = 25),
            DailyTask(userId = userId, taskType = "REFER_FRIEND", title = "Refer 1 Friend", targetCount = 1, currentCount = 0, rewardCoins = 15)
        )
        repository.insertDailyTasks(tasks)
    }

    // --- TOURNAMENT OPERATIONS ---
    fun joinTournament(tournamentId: Int) {
        val user = _currentUser.value ?: run {
            triggerToast("You must log in first!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val t = repository.getTournamentById(tournamentId) ?: run {
                triggerToast("Tournament not found!")
                return@launch
            }

            // Validation rules
            if (t.status != "Open") {
                triggerToast("Registration is closed or not open yet!")
                return@launch
            }

            if (t.filledSlots >= t.totalSlots) {
                triggerToast("Tournament slots are completely full!")
                return@launch
            }

            // Duplicate join check
            val alreadyJoined = repository.getJoinedByUserAndTournament(tournamentId, user.id)
            if (alreadyJoined != null) {
                triggerToast("You have already joined this tournament!")
                return@launch
            }

            val totalCashBalance = user.mainWallet + user.bonusWallet + user.winningWallet
            if (totalCashBalance < t.entryFee) {
                triggerToast("Insufficient wallet balance! Entry fee: ${t.entryFee}")
                return@launch
            }

            // Deduct Fee
            var remainingFee = t.entryFee
            var deductedMain = 0.0
            var deductedWinning = 0.0
            var deductedBonus = 0.0

            val currentBonus = user.bonusWallet
            val currentMain = user.mainWallet
            val currentWinning = user.winningWallet

            // Deduct up to 50% or maximum available from bonus wallet first as a discount
            val maxBonusDeductMultiplier = 0.2 // E.g. deduct up to 20% entry fee from bonus
            val possibleBonusDeduct = t.entryFee * maxBonusDeductMultiplier
            val bonusDeduction = if (currentBonus >= possibleBonusDeduct) possibleBonusDeduct else currentBonus
            
            deductedBonus = bonusDeduction
            remainingFee -= bonusDeduction

            if (remainingFee > 0.0) {
                if (currentMain >= remainingFee) {
                    deductedMain = remainingFee
                    remainingFee = 0.0
                } else {
                    deductedMain = currentMain
                    remainingFee -= currentMain

                    if (currentWinning >= remainingFee) {
                        deductedWinning = remainingFee
                        remainingFee = 0.0
                    } else {
                        triggerToast("Error matching currency streams.")
                        return@launch
                    }
                }
            }

            // Update user balance
            val updatedUser = user.copy(
                mainWallet = currentMain - deductedMain,
                winningWallet = currentWinning - deductedWinning,
                bonusWallet = currentBonus - deductedBonus,
                matchesPlayed = user.matchesPlayed + 1
            )
            repository.updateUser(updatedUser)

            // Increment Slots in Tournament
            val updatedTournament = t.copy(
                filledSlots = t.filledSlots + 1,
                status = if (t.filledSlots + 1 >= t.totalSlots) "Full" else "Open"
            )
            repository.updateTournament(updatedTournament)

            // Insert Registration Record
            repository.insertJoinedTournament(JoinedTournament(
                tournamentId = t.id,
                userId = user.id,
                dateJoined = System.currentTimeMillis()
            ))

            // Log Transaction history
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = t.entryFee,
                type = "Entry Fee Deduction",
                walletType = "Multi-Wallet (${deductedMain}M/${deductedWinning}W/${deductedBonus}B)",
                status = "Completed",
                description = "Joined Tournament \"${t.name}\"",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            // Create Reminder Notification
            repository.insertNotification(Notification(
                userId = user.id,
                title = "Joined: ${t.name}",
                message = "Success! Entry fee of ${t.entryFee} PKR deducted. Check Room credentials 10 mins before match start time.",
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Tournament Reminder"
            ))

            // Progress Daily Task: "Join 1 Tournament"
            incrementTaskProgress(user.id, "JOIN_TOURNAMENT")

            triggerToast("Successfully registered for ${t.name}!")
        }
    }

    // --- REWARDS SYSTEM ---
    fun claimDailyReward(dayNum: Int, rewardCoins: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val lastClaim = repository.getClaimForUser(user.id)
            val currentTime = System.currentTimeMillis()

            if (lastClaim != null) {
                // Check if last claim was on the same calendar day
                if (isSameDay(lastClaim.lastClaimedTime, currentTime)) {
                    triggerToast("Daily Reward already claimed today!")
                    return@launch
                }

                // If last claimed was exactly yesterday, verify or update streak. Otherwise reset
                val isYesterday = isYesterday(lastClaim.lastClaimedTime)
                val newStreak = if (isYesterday) {
                    val streak = (lastClaim.claimStreak % 7) + 1
                    if (streak != dayNum) {
                        // user can only claim the next logical day
                        dayNum
                    } else streak
                } else {
                    1 // streak broken
                }

                repository.insertDailyClaim(DailyRewardClaim(user.id, currentTime, newStreak))
            } else {
                repository.insertDailyClaim(DailyRewardClaim(user.id, currentTime, dayNum))
            }

            // Credit Coins
            val updatedUser = user.copy(coins = user.coins + rewardCoins)
            repository.updateUser(updatedUser)

            // Log Coin Tx History
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = rewardCoins.toDouble(),
                type = "Daily Reward",
                walletType = "Coins",
                status = "Completed",
                description = "Day $dayNum Claim Rewards",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            repository.insertNotification(Notification(
                userId = user.id,
                title = "Daily Reward Claimed",
                message = "Congratulations! You claimed your Day $dayNum reward of $rewardCoins Coins.",
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Rewards Available"
            ))

            triggerToast("Claimed Day $dayNum reward: $rewardCoins Coins added!")
        }
    }

    // --- DAILY TASKS SYSTEM ---
    fun incrementTaskProgress(userId: Int, taskType: String, amount: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = repository.getTasksForUserList(userId)
            val matching = tasks.find { it.taskType == taskType }
            if (matching != null && !matching.isCompleted) {
                val newCount = matching.currentCount + amount
                val isCompNow = newCount >= matching.targetCount
                val updated = matching.copy(
                    currentCount = if (newCount > matching.targetCount) matching.targetCount else newCount,
                    isCompleted = isCompNow
                )
                repository.updateDailyTask(updated)

                if (isCompNow) {
                    repository.insertNotification(Notification(
                        userId = userId,
                        title = "Task Completed!",
                        message = "You completed: \"${matching.title}\". Claim your reward now!",
                        date = getCurrentDateString(),
                        time = getCurrentTimeString(),
                        type = "Rewards Available"
                    ))
                }
            }
        }
    }

    fun claimTaskReward(task: DailyTask) {
        if (!task.isCompleted || task.isClaimed) return
        val user = _currentUser.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            // Update user coins
            val updatedUser = user.copy(coins = user.coins + task.rewardCoins)
            repository.updateUser(updatedUser)

            // Update task claimed status
            repository.updateDailyTask(task.copy(isClaimed = true))

            // Log Transaction
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = task.rewardCoins.toDouble(),
                type = "Task Reward",
                walletType = "Coins",
                status = "Completed",
                description = "Completed Daily Task: ${task.title}",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            triggerToast("Claimed ${task.rewardCoins} Coins for Daily Task!")
        }
    }

    // --- WALLET: DEPOSIT & WITHDRAW DEPOSIT REQUESTS ---
    fun submitDepositRequest(amount: Double, method: String, transactionId: String, screenshotUri: String?) {
        val user = _currentUser.value ?: return
        if (amount <= 0) {
            triggerToast("Enter a valid amount!")
            return
        }
        if (transactionId.isEmpty()) {
            triggerToast("Enter the Transaction ID!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDepositRequest(DepositRequest(
                userId = user.id,
                username = user.username,
                amount = amount,
                paymentMethod = method,
                transactionId = transactionId,
                screenshotUri = screenshotUri,
                status = "Pending",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            triggerToast("Deposit request submitted successfully for approval!")
            
            // Create Transaction entry (Pending status)
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = amount,
                type = "Deposit",
                walletType = "Main Wallet",
                status = "Pending",
                description = "Deposit via $method (Tx: $transactionId)",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))
        }
    }

    fun submitWithdrawRequest(amount: Double, accountName: String, accountNumber: String, paymentMethod: String) {
        val user = _currentUser.value ?: return
        if (amount <= 0) {
            triggerToast("Enter a valid withdrawal amount!")
            return
        }
        if (user.winningWallet < amount) {
            triggerToast("Insufficient winning wallet balance of ${user.winningWallet}")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Debit or lock the user amount from winning wallet immediately to prevent overclaim
            val updatedUser = user.copy(
                winningWallet = user.winningWallet - amount
            )
            repository.updateUser(updatedUser)

            repository.insertWithdrawalRequest(WithdrawalRequest(
                userId = user.id,
                username = user.username,
                amount = amount,
                accountName = accountName,
                accountNumber = accountNumber,
                paymentMethod = paymentMethod,
                status = "Pending",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            // Create Transaction History Log (Pending)
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = amount,
                type = "Withdrawal",
                walletType = "Winning Wallet",
                status = "Pending",
                description = "Withdraw to $accountName (Acc: $accountNumber)",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            triggerToast("Withdrawal request submitted! Amount: $amount locked.")
        }
    }

    // --- BUY STORE COINS PACKAGE ---
    fun purchaseCoinsPackage(pkg: CoinPackage) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val cost = pkg.price
            if (user.mainWallet < cost) {
                triggerToast("Insufficient wallet balance to purchase this package! Deposit PKR first.")
                return@launch
            }

            // Deduct Cash and Credit Coins (Coins + Bonus)
            val updatedUser = user.copy(
                mainWallet = user.mainWallet - cost,
                coins = user.coins + pkg.coins + pkg.bonusCoins
            )
            repository.updateUser(updatedUser)

            // Log transaction
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = cost,
                type = "Coin Package Purchase",
                walletType = "Main Wallet",
                status = "Completed",
                description = "Purchased Product \"${pkg.name}\"",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            repository.insertNotification(Notification(
                userId = user.id,
                title = "Coins Package Purchased",
                message = "Added ${pkg.coins + pkg.bonusCoins} Coins to your profile after buying ${pkg.name}.",
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Announcement"
            ))

            triggerToast("Purchased ${pkg.name}! Credits added.")
        }
    }

    // --- PROFILE AND REFERRAL REDEMPTION ---
    fun saveUserProfile(username: String, profileImage: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = user.copy(
                username = username,
                profileImage = profileImage
            )
            repository.updateUser(updated)
            triggerToast("Profile updated successfully!")
        }
    }

    // --- GAME MINUTES SIMULATION FOR TASKS (TESTING SUITE) ---
    fun simulateGamePlayMinutes(minutes: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            incrementTaskProgress(user.id, "PLAY_2", minutes)
            incrementTaskProgress(user.id, "PLAY_15", minutes)
            incrementTaskProgress(user.id, "PLAY_20", minutes)
            triggerToast("Simulated $minutes minutes of gameplay. Check Tasks progress!")
        }
    }

    // --- REFERRAL CODE CLAIM ---
    fun applyReferralCode(code: String) {
        val user = _currentUser.value ?: return
        if (user.referralCode == code) {
            triggerToast("You cannot use your own referral code!")
            return
        }
        if (!user.referredBy.isNullOrEmpty()) {
            triggerToast("You have already used a referral code!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val referrer = repository.getUserByReferralCode(code)
            if (referrer == null) {
                triggerToast("Invalid Referral Code!")
                return@launch
            }

            // Valid referrer found!
            // Update current user
            val updatedUser = user.copy(
                referredBy = code,
                mainWallet = user.mainWallet + 10.0, // bonus reward
                coins = user.coins + 15
            )
            repository.updateUser(updatedUser)

            // Log current user referral transaction
            repository.insertTransactionHistory(TransactionHistory(
                userId = user.id,
                amount = 10.0,
                type = "Referral Bonus",
                walletType = "Main Wallet",
                status = "Completed",
                description = "Applied Referral code from ${referrer.username}",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            repository.insertNotification(Notification(
                userId = user.id,
                title = "Referral Discount Applied",
                message = "You received 10.0 PKR and 15 Coins for using referral code.",
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Rewards Available"
            ))

            // Update referrer
            val updatedReferrer = referrer.copy(
                mainWallet = referrer.mainWallet + 15.0,
                referralCount = referrer.referralCount + 1,
                coins = referrer.coins + 20
            )
            repository.updateUser(updatedReferrer)

            repository.insertTransactionHistory(TransactionHistory(
                userId = referrer.id,
                amount = 15.0,
                type = "Referral Bonus",
                walletType = "Main Wallet",
                status = "Completed",
                description = "Referral code used by ${user.username}",
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            ))

            repository.insertNotification(Notification(
                userId = referrer.id,
                title = "Referral Reward Credited",
                message = "You received 15.0 PKR and 20 Coins because ${user.username} joined using your code.",
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Rewards Available"
            ))

            // Progress task
            incrementTaskProgress(referrer.id, "REFER_FRIEND")

            triggerToast("Referral applied successfully! 10.0 PKR added.")
        }
    }


    // ═══════════════════════════════════════
    //   HIDDEN ADMIN PANEL ACTIONS
    // ═══════════════════════════════════════

    // -- User Management --
    fun adminBanUnbanUser(userId: Int, ban: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(userId) ?: return@launch
            repository.updateUser(user.copy(isBanned = ban))
            triggerToast(if (ban) "User Banned Successfully!" else "User Unbanned Successfully!")
        }
    }

    fun adminEditWalletBalance(userId: Int, main: Double, winning: Double, bonus: Double, coins: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(userId) ?: return@launch
            val updated = user.copy(
                mainWallet = main,
                winningWallet = winning,
                bonusWallet = bonus,
                coins = coins
            )
            repository.updateUser(updated)
            triggerToast("User balances updated successfully!")

            repository.insertNotification(Notification(
                userId = userId,
                title = "Wallet Balance Overridden",
                message = "An Admin has adjusted your wallet levels. Main: $main, Winning: $winning, Bonus: $bonus. Coins: $coins.",
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Announcement"
            ))
        }
    }

    fun adminDeleteUser(userName: String, userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(userId) ?: return@launch
            repository.deleteUser(user)
            triggerToast("Deleted user $userName")
        }
    }

    // -- Tournament Management --
    fun adminCreateTournament(tournament: Tournament) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTournament(tournament)
            triggerToast("Tournament Created: ${tournament.name}")
        }
    }

    fun adminUpdateTournament(tournament: Tournament) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTournament(tournament)
            triggerToast("Tournament Updated: ${tournament.name}")
        }
    }

    fun adminDeleteTournament(tournament: Tournament) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTournament(tournament)
            triggerToast("Tournament Deleted!")
        }
    }

    // -- Banner Management --
    fun adminCreateBanner(banner: BannerBanner) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBanner(banner)
            triggerToast("Banner Created!")
        }
    }

    fun adminDeleteBanner(banner: BannerBanner) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBanner(banner)
            triggerToast("Banner Deleted!")
        }
    }

    // -- Deposit Approvals --
    fun adminApproveDeposit(requestId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val req = repository.getDepositById(requestId) ?: return@launch
            if (req.status != "Pending") return@launch

            // Approve request
            repository.updateDepositRequest(req.copy(status = "Approved"))

            // Find user, add balance
            val user = repository.getUserById(req.userId)
            if (user != null) {
                repository.updateUser(user.copy(
                    mainWallet = user.mainWallet + req.amount
                ))

                // Insert notifications
                repository.insertNotification(Notification(
                    userId = user.id,
                    title = "Deposit Approved!",
                    message = "Your deposit of ${req.amount} PKR has been approved and credited to your Main Wallet.",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString(),
                    type = "Deposit Approved"
                ))

                // Log final transaction status
                repository.insertTransactionHistory(TransactionHistory(
                    userId = user.id,
                    amount = req.amount,
                    type = "Deposit Approved",
                    walletType = "Main Wallet",
                    status = "Completed",
                    description = "Deposit Approved (Tx: ${req.transactionId})",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString()
                ))
            }
            triggerToast("Deposit Approved!")
        }
    }

    fun adminRejectDeposit(requestId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val req = repository.getDepositById(requestId) ?: return@launch
            if (req.status != "Pending") return@launch

            repository.updateDepositRequest(req.copy(status = "Rejected"))

            val user = repository.getUserById(req.userId)
            if (user != null) {
                repository.insertNotification(Notification(
                    userId = user.id,
                    title = "Deposit REJECTED",
                    message = "Your deposit request for ${req.amount} PKR has been rejected. Details matched: Invalid Transation Code.",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString(),
                    type = "Announcement"
                ))

                // Transaction history update
                repository.insertTransactionHistory(TransactionHistory(
                    userId = user.id,
                    amount = req.amount,
                    type = "Deposit Rejected",
                    walletType = "Main Wallet",
                    status = "Rejected",
                    description = "Deposit Rejected (Tx: ${req.transactionId})",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString()
                ))
            }
            triggerToast("Deposit Rejected!")
        }
    }

    // -- Withdrawal Approvals --
    fun adminApproveWithdrawal(requestId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val req = repository.getWithdrawalById(requestId) ?: return@launch
            if (req.status != "Pending") return@launch

            repository.updateWithdrawalRequest(req.copy(status = "Approved"))

            val user = repository.getUserById(req.userId)
            if (user != null) {
                repository.insertNotification(Notification(
                    userId = user.id,
                    title = "Withdrawal Disbursed",
                    message = "Success! Withdrawal of ${req.amount} PKR approved and wired to ${req.paymentMethod} account.",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString(),
                    type = "Withdraw Approved"
                ))

                repository.insertTransactionHistory(TransactionHistory(
                    userId = user.id,
                    amount = req.amount,
                    type = "Withdrawal Approved",
                    walletType = "Winning Wallet",
                    status = "Completed",
                    description = "Withdraw approved to ${req.accountName}",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString()
                ))
            }
            triggerToast("Withdrawal Approved!")
        }
    }

    fun adminRejectWithdrawal(requestId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val req = repository.getWithdrawalById(requestId) ?: return@launch
            if (req.status != "Pending") return@launch

            repository.updateWithdrawalRequest(req.copy(status = "Rejected"))

            // Refund locked amount
            val user = repository.getUserById(req.userId)
            if (user != null) {
                repository.updateUser(user.copy(
                    winningWallet = user.winningWallet + req.amount
                ))

                repository.insertNotification(Notification(
                    userId = user.id,
                    title = "Withdrawal REJECTED (Refunded)",
                    message = "Your withdrawal of ${req.amount} PKR was rejected. Balance refunded to your winning wallet.",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString(),
                    type = "Announcement"
                ))

                repository.insertTransactionHistory(TransactionHistory(
                    userId = user.id,
                    amount = req.amount,
                    type = "Withdrawal Rejected",
                    walletType = "Winning Wallet",
                    status = "Rejected",
                    description = "Withdraw rejected — ${req.amount} Refunded",
                    date = getCurrentDateString(),
                    time = getCurrentTimeString()
                ))
            }
            triggerToast("Withdrawal Rejected & Refunded!")
        }
    }

    // -- Global Admin Announcements --
    fun adminPostNotification(title: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNotification(Notification(
                userId = null, // Broadcast to all
                title = title,
                message = message,
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                type = "Announcement"
            ))
            triggerToast("Announcement posted!")
        }
    }


    // ═══════════════════════════════════════
    //   DATABASE SEED DATA POPULATOR
    // ═══════════════════════════════════════
    private fun seedDatabaseIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllTournamentsList()
            if (list.isEmpty()) {
                // Seed settings
                repository.insertSetting(AppSetting("app_name", "Anu Battle"))
                repository.insertSetting(AppSetting("support_contact", "support@anubattle.com"))
                repository.insertSetting(AppSetting("referral_bonus", "15"))

                // Seed Packages
                repository.insertCoinPackage(CoinPackage(name = "Coins Pack S", price = 10.0, description = "Credit 20 coins for instant play setup", coins = 20, bonusCoins = 2))
                repository.insertCoinPackage(CoinPackage(name = "Coins Pack M", price = 25.0, description = "Value pack of 55 coins + bonus credits", coins = 55, bonusCoins = 8))
                repository.insertCoinPackage(CoinPackage(name = "Coins Pack L", price = 50.0, description = "Clash bundle of 120 coins", coins = 120, bonusCoins = 25))
                repository.insertCoinPackage(CoinPackage(name = "Champion Special", price = 100.0, description = "Huge pro stack of 250 coins with double multipliers", coins = 250, bonusCoins = 60))

                // Seed Banners
                repository.insertBanner(BannerBanner(title = "BGMI Esports League Is Live!", description = "Assemble your squad, lock in your registers, and play for a massive 10,000 PKR Cash pool today.", imageUrl = "banner_bgmi"))
                repository.insertBanner(BannerBanner(title = "FreeFire Solo Bermuda Arena", description = "Fight alone or perish in silence. Join our custom direct lobby now for only 15 PKR.", imageUrl = "banner_ff"))
                repository.insertBanner(BannerBanner(title = "Refer Your Buddies & Earn Real Cash!", description = "Share your unique referral link to claim 15.0 PKR Main wallet cash instantly.", imageUrl = "banner_refer"))

                // Seed Default Tournaments
                val t1 = Tournament(
                    name = "BGMI Clash Squad Showdown",
                    type = "Clash Squad",
                    entryFee = 15.0,
                    prizePool = 1200.0,
                    totalSlots = 16,
                    filledSlots = 12,
                    date = getFutureDateString(1),
                    time = "14:30",
                    status = "Open",
                    format = "Squad",
                    mapType = "Erangel",
                    roomType = "Clash Squad",
                    description = "The ultimate clash squad tournament for BGMI custom lobbies. Standard custom squad configurations apply. Verify client specs before registers.",
                    rules = "1. Emulator players strictly blocked. 2. Custom rooms open 15 mins early. 3. Friendly fire and macros count as instantly disqualifying.",
                    killReward = 2.0,
                    rankReward = "1st Place: 600 PKR, 2nd Place: 400 PKR, 3rd Place: 200 PKR",
                    roomId = "8429104",
                    roomPassword = "anu_battle_squad",
                    visibilityMode = "Scheduled"
                )
                repository.insertTournament(t1)

                val t2 = Tournament(
                    name = "Free Fire Solo Survival Arena",
                    type = "Battle Royale",
                    entryFee = 10.0,
                    prizePool = 800.0,
                    totalSlots = 50,
                    filledSlots = 48,
                    date = getFutureDateString(1),
                    time = "18:00",
                    status = "Open",
                    format = "Solo",
                    mapType = "Bermuda",
                    roomType = "Classic",
                    description = "Take the challenge and survive Bermuda. The last player alive claims the crown of the arena.",
                    rules = "1. Solo matchmaking rules. 2. Any use of game exploits will yield a permanent system ban. 3. Must fill room details 10 minutes prior.",
                    killReward = 1.5,
                    rankReward = "1st Place: 400 PKR, 2nd Place: 250 PKR, 3rd Place: 150 PKR",
                    roomId = "3948119",
                    roomPassword = "anu_battle_ff",
                    visibilityMode = "Permanent"
                )
                repository.insertTournament(t2)

                val t3 = Tournament(
                    name = "CODM Lone Wolf Duel League",
                    type = "Lone Wolf",
                    entryFee = 20.0,
                    prizePool = 1500.0,
                    totalSlots = 8,
                    filledSlots = 8,
                    date = getFutureDateString(2),
                    time = "12:00",
                    status = "Full",
                    format = "Solo",
                    mapType = "Nuketown",
                    roomType = "Classic",
                    description = "One-on-one duel arena. Show off raw aiming precision and win with 100% solo earnings.",
                    rules = "1. Custom standard rules. 2. All weapons allowed. 3. First to 15 kills wins.",
                    killReward = 5.0,
                    rankReward = "1st: 1000 PKR, 2nd: 500 PKR",
                    roomId = "1029415",
                    roomPassword = "co_duel_battle",
                    visibilityMode = "Scheduled"
                )
                repository.insertTournament(t3)

                val t4 = Tournament(
                    name = "Apex Custom Legends Cup",
                    type = "Custom Tournament",
                    entryFee = 25.0,
                    prizePool = 2500.0,
                    totalSlots = 20,
                    filledSlots = 14,
                    date = getFutureDateString(3),
                    time = "20:00",
                    status = "Upcoming",
                    format = "Duo",
                    mapType = "World's Edge",
                    roomType = "Custom",
                    rules = "All tactical features enabled. Real-time logging active.",
                    description = "Premium Apex Legends Duo lobbies. High reward ratios with intense custom server speeds.",
                    killReward = 3.0,
                    rankReward = "1st Place: 1500 PKR, 2nd Place: 1000 PKR",
                    roomId = "8829555",
                    roomPassword = "apex_anu_legends",
                    visibilityMode = "Scheduled"
                )
                repository.insertTournament(t4)

                val t5 = Tournament(
                    name = "BGMI Squad Master Series S1",
                    type = "Squad Tournament",
                    entryFee = 30.0,
                    prizePool = 5000.0,
                    totalSlots = 25,
                    filledSlots = 25,
                    date = getCurrentDateString(),
                    time = "10:00",
                    status = "Completed",
                    format = "Squad",
                    mapType = "Sanhok",
                    roomType = "Classic",
                    description = "Finished League 1 series. Match concluded.",
                    rules = "Standard Pro Rules.",
                    killReward = 5.0,
                    rankReward = "1st: 2500 PKR, 2nd: 1500 PKR, 3rd: 1000 PKR",
                    roomId = "5592817",
                    roomPassword = "concluded_master_match",
                    visibilityMode = "Permanent"
                )
                repository.insertTournament(t5)
            }
        }
    }

    // --- DATE/TIME CONVERT UTILS ---
    fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getFutureDateString(plusDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, plusDays)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val s1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(t1))
        val s2 = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(t2))
        return s1 == s2
    }

    private fun isYesterday(time: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sYesterday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        val sTime = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(time))
        return sYesterday == sTime
    }

    // --- FIREBASE SYNC INTEGRATION ---
    fun syncFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://free-fire-tour-bee66-default-rtdb.firebaseio.com/.json")
                    .build()
                
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    
                    // 1. Sync settings and parameters
                    if (json.has("settings")) {
                        val settingsObj = json.getJSONObject("settings")
                        val settingsMap = jsonObjectToMap(settingsObj)
                        for ((key, value) in settingsMap) {
                            if (value != null) {
                                repository.insertSetting(AppSetting(key, value.toString()))
                            }
                        }
                    }

                    // 2. Sync Banners/Promotions
                    if (json.has("promotions")) {
                        val promosObj = json.getJSONObject("promotions")
                        val promosMap = jsonObjectToMap(promosObj)
                        for ((fbId, data) in promosMap) {
                            if (data is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val banner = mapFirebaseBanner(fbId, data as Map<String, Any?>)
                                repository.insertBanner(banner)
                            }
                        }
                    }

                    // 3. Sync Tournaments
                    if (json.has("tournaments")) {
                        val toursObj = json.getJSONObject("tournaments")
                        val toursMap = jsonObjectToMap(toursObj)
                        for ((fbId, data) in toursMap) {
                            if (data is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val tournament = mapFirebaseTournament(fbId, data as Map<String, Any?>)
                                repository.insertTournament(tournament)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var value = jsonObject.get(key)
            if (value is JSONObject) {
                value = jsonObjectToMap(value)
            } else if (value is JSONArray) {
                value = jsonArrayToList(value)
            } else if (value == JSONObject.NULL) {
                value = null
            }
            map[key] = value
        }
        return map
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<Any?> {
        val list = ArrayList<Any?>()
        for (i in 0 until jsonArray.length()) {
            var value = jsonArray.get(i)
            if (value is JSONObject) {
                value = jsonObjectToMap(value)
            } else if (value is JSONArray) {
                value = jsonArrayToList(value)
            } else if (value == JSONObject.NULL) {
                value = null
            }
            list.add(value)
        }
        return list
    }

    private fun mapFirebaseTournament(fbId: String, map: Map<String, Any?>): Tournament {
        val name = map["name"] as? String ?: "Tournament"
        val description = map["description"] as? String ?: ""
        val entryFee = (map["entryFee"] as? Number)?.toDouble() ?: (map["entryFeeCoins"] as? Number)?.toDouble() ?: 0.0
        val prizePool = (map["prizePool"] as? Number)?.toDouble() ?: (map["prizePoolCoins"] as? Number)?.toDouble() ?: 0.0
        val maxPlayers = (map["maxPlayers"] as? Number)?.toInt() ?: 100
        
        val startTime = (map["startTime"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startTime))
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startTime))
        
        val type = map["type"] as? String ?: "Battle Royale"
        val status = map["status"] as? String ?: "Open"
        
        val registeredPlayersMap = map["registeredPlayers"] as? Map<*, *>
        val filledSlots = registeredPlayersMap?.size ?: 0
        
        val format = (map["tags"] as? List<*>)?.firstOrNull() as? String ?: "Solo"
        val roomId = map["roomId"] as? String ?: ""
        val roomPassword = map["roomPassword"] as? String ?: ""
        
        val localId = Math.abs(fbId.hashCode())
        
        return Tournament(
            id = localId,
            name = name,
            type = type,
            entryFee = entryFee,
            prizePool = prizePool,
            totalSlots = maxPlayers,
            filledSlots = if (filledSlots > maxPlayers) maxPlayers else filledSlots,
            date = dateStr,
            time = timeStr,
            status = if (status.isEmpty()) "Open" else status,
            format = format,
            description = description,
            roomId = roomId,
            roomPassword = roomPassword
        )
    }

    private fun mapFirebaseBanner(fbId: String, map: Map<String, Any?>): BannerBanner {
        val title = map["title"] as? String ?: "Promotion"
        val description = map["description"] as? String ?: ""
        val imageUrl = map["imageUrl"] as? String ?: ""
        val link = map["link"] as? String ?: ""
        return BannerBanner(
            id = Math.abs(fbId.hashCode()),
            title = title,
            description = description,
            imageUrl = imageUrl,
            actionUrl = link
        )
    }
}
