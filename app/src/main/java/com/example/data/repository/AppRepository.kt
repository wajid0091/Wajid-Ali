package com.example.data.repository

import com.example.data.database.AppDao
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- USER ---
    val allUsers: Flow<List<User>> = appDao.getAllUsersFlow()
    
    suspend fun getAllUsersList() = appDao.getAllUsers()
    suspend fun getUserById(id: Int): User? = appDao.getUserById(id)
    fun getUserByIdFlow(id: Int): Flow<User?> = appDao.getUserByIdFlow(id)
    suspend fun getUserByEmail(email: String): User? = appDao.getUserByEmail(email)
    suspend fun getUserByReferralCode(code: String): User? = appDao.getUserByReferralCode(code)
    suspend fun insertUser(user: User): Long = appDao.insertUser(user)
    suspend fun updateUser(user: User) = appDao.updateUser(user)
    suspend fun deleteUser(user: User) = appDao.deleteUser(user)

    // --- TOURNAMENT ---
    val allTournaments: Flow<List<Tournament>> = appDao.getAllTournamentsFlow()
    
    suspend fun getAllTournamentsList() = appDao.getAllTournaments()
    suspend fun getTournamentById(id: Int): Tournament? = appDao.getTournamentById(id)
    fun getTournamentByIdFlow(id: Int): Flow<Tournament?> = appDao.getTournamentByIdFlow(id)
    suspend fun insertTournament(tournament: Tournament): Long = appDao.insertTournament(tournament)
    suspend fun updateTournament(tournament: Tournament) = appDao.updateTournament(tournament)
    suspend fun deleteTournament(tournament: Tournament) = appDao.deleteTournament(tournament)

    // --- JOINED TOURNAMENT ---
    val allJoined: Flow<List<JoinedTournament>> = appDao.getAllJoinedFlow()
    
    fun getJoinedTournamentsForUser(userId: Int): Flow<List<JoinedTournament>> =
        appDao.getJoinedTournamentsForUserFlow(userId)

    suspend fun getJoinedTournamentsForUserList(userId: Int) = appDao.getJoinedTournamentsForUser(userId)

    suspend fun getJoinedForTournament(tournamentId: Int): List<JoinedTournament> =
        appDao.getJoinedForTournament(tournamentId)

    fun getJoinedForTournamentFlow(tournamentId: Int): Flow<List<JoinedTournament>> =
        appDao.getJoinedForTournamentFlow(tournamentId)

    suspend fun getJoinedByUserAndTournament(tournamentId: Int, userId: Int): JoinedTournament? =
        appDao.getJoinedByUserAndTournament(tournamentId, userId)

    suspend fun insertJoinedTournament(joined: JoinedTournament): Long =
        appDao.insertJoinedTournament(joined)

    suspend fun updateJoinedTournament(joined: JoinedTournament) =
        appDao.updateJoinedTournament(joined)

    suspend fun deleteJoinedTournament(joined: JoinedTournament) =
        appDao.deleteJoinedTournament(joined)

    // --- DEPOSIT REQUEST ---
    val allDeposits: Flow<List<DepositRequest>> = appDao.getAllDepositsFlow()
    
    fun getDepositsForUser(userId: Int): Flow<List<DepositRequest>> =
        appDao.getDepositsForUserFlow(userId)

    suspend fun getDepositById(id: Int): DepositRequest? = appDao.getDepositById(id)
    suspend fun insertDepositRequest(deposit: DepositRequest): Long =
        appDao.insertDepositRequest(deposit)

    suspend fun updateDepositRequest(deposit: DepositRequest) =
        appDao.updateDepositRequest(deposit)

    // --- WITHDRAW REQUEST ---
    val allWithdrawals: Flow<List<WithdrawalRequest>> = appDao.getAllWithdrawalsFlow()
    
    fun getWithdrawalsForUser(userId: Int): Flow<List<WithdrawalRequest>> =
        appDao.getWithdrawalsForUserFlow(userId)

    suspend fun getWithdrawalById(id: Int): WithdrawalRequest? = appDao.getWithdrawalById(id)
    suspend fun insertWithdrawalRequest(withdrawal: WithdrawalRequest): Long =
        appDao.insertWithdrawalRequest(withdrawal)

    suspend fun updateWithdrawalRequest(withdrawal: WithdrawalRequest) =
        appDao.updateWithdrawalRequest(withdrawal)

    // --- DAILY CLAIM ---
    suspend fun getClaimForUser(userId: Int): DailyRewardClaim? = appDao.getClaimForUser(userId)
    suspend fun insertDailyClaim(claim: DailyRewardClaim) = appDao.insertDailyClaim(claim)

    // --- DAILY TASKS ---
    fun getTasksForUserFlow(userId: Int): Flow<List<DailyTask>> = appDao.getTasksForUserFlow(userId)
    suspend fun getTasksForUserList(userId: Int): List<DailyTask> = appDao.getTasksForUser(userId)
    suspend fun insertDailyTask(task: DailyTask) = appDao.insertDailyTask(task)
    suspend fun insertDailyTasks(tasks: List<DailyTask>) = appDao.insertDailyTasks(tasks)
    suspend fun updateDailyTask(task: DailyTask) = appDao.updateDailyTask(task)
    suspend fun deleteTasksForUser(userId: Int) = appDao.deleteTasksForUser(userId)

    // --- NOTIFICATIONS ---
    val allNotifications: Flow<List<Notification>> = appDao.getAllNotificationsFlow()
    
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>> =
        appDao.getNotificationsForUserFlow(userId)

    suspend fun insertNotification(notification: Notification): Long =
        appDao.insertNotification(notification)

    suspend fun updateNotification(notification: Notification) =
        appDao.updateNotification(notification)

    suspend fun deleteNotificationById(id: Int) = appDao.deleteNotificationById(id)

    // --- TRANSACTION HISTORY ---
    fun getTransactionsForUser(userId: Int): Flow<List<TransactionHistory>> =
        appDao.getTransactionsForUserFlow(userId)

    suspend fun insertTransactionHistory(tx: TransactionHistory): Long =
        appDao.insertTransactionHistory(tx)

    // --- BANNERS ---
    val allBanners: Flow<List<BannerBanner>> = appDao.getAllBannersFlow()
    
    suspend fun insertBanner(banner: BannerBanner): Long = appDao.insertBanner(banner)
    suspend fun deleteBanner(banner: BannerBanner) = appDao.deleteBanner(banner)

    // --- SETTINGS ---
    val allSettings: Flow<List<AppSetting>> = appDao.getAllSettingsFlow()
    
    suspend fun getSetting(key: String): AppSetting? = appDao.getSetting(key)
    suspend fun insertSetting(setting: AppSetting) = appDao.insertSetting(setting)

    // --- COIN PACKAGES ---
    val allCoinPackages: Flow<List<CoinPackage>> = appDao.getAllCoinPackagesFlow()
    
    suspend fun insertCoinPackage(cp: CoinPackage): Long = appDao.insertCoinPackage(cp)
    suspend fun deleteCoinPackage(cp: CoinPackage) = appDao.deleteCoinPackage(cp)
}
