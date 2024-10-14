package io.rebble.cobble.shared.ui.common

import android.shared.generated.resources.RebbleIcons
import android.shared.generated.resources.Res
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Font


object RebbleIcons {
    @Composable
    fun font() = FontFamily(Font(Res.font.RebbleIcons))

    @Composable
    fun floppyDiskHealthDatabase(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe800), modifier = modifier)
    @Composable
    fun floppyDisk(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe801), modifier = modifier)
    @Composable
    fun silencePhone(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe802), modifier = modifier)
    @Composable
    fun vibratingWatch(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe803), modifier = modifier)
    @Composable
    fun watchApps(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe804), modifier = modifier)
    @Composable
    fun watchFaces(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe805), modifier = modifier)
    @Composable
    fun healthJournal(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe806), modifier = modifier)
    @Composable
    fun healthHeart(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe807), modifier = modifier)
    @Composable
    fun healthSleep(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe808), modifier = modifier)
    @Composable
    fun healthSteps(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe809), modifier = modifier)
    @Composable
    fun analytics(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe80a), modifier = modifier)
    @Composable
    fun developerSettings(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe80b), modifier = modifier)
    @Composable
    fun smsMessages(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe80c), modifier = modifier)
    @Composable
    fun rocket(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe80d), modifier = modifier)
    @Composable
    fun unpairFromWatch(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe80e), modifier = modifier)
    @Composable
    fun applyUpdate(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe80f), modifier = modifier)
    @Composable
    fun checkForUpdates(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe810), modifier = modifier)
    @Composable
    fun disconnectFromWatch(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe811), modifier = modifier)
    @Composable
    fun connectToWatch(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe812), modifier = modifier)
    @Composable
    fun devices(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe813), modifier = modifier)
    @Composable
    fun plusAdd(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe814), modifier = modifier)
    @Composable
    fun search(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe815), modifier = modifier, contentDescription = "Search")
    @Composable
    fun dictationMicrophone(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe816), modifier = modifier)
    @Composable
    fun systemLanguage(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe817), modifier = modifier)
    @Composable
    fun aboutApp(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe818), modifier = modifier)
    @Composable
    fun share(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe819), modifier = modifier)
    @Composable
    fun developerConnectionConsole(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe81a), modifier = modifier)
    @Composable
    fun rebbleStore(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe81b), modifier = modifier)
    @Composable
    fun settings(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe81c), modifier = modifier)
    @Composable
    fun notification(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe81d), "Notification", modifier = modifier)
    @Composable
    fun unknownApp(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe81e), "Unknown app", modifier = modifier)
    @Composable
    fun locker(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe81f), "Locker", modifier = modifier)
    @Composable
    fun sendToWatchChecked(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe821), modifier = modifier)
    @Composable
    fun timelinePin(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe822), modifier = modifier)
    @Composable
    fun menuHorizontal(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe823), modifier = modifier)
    @Composable
    fun menuVertical(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe824), modifier = modifier)
    @Composable
    fun checkDone(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe825), modifier = modifier)
    @Composable
    fun xClose(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe826), modifier = modifier)
    @Composable
    fun caretUp(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe827), modifier = modifier)
    @Composable
    fun caretDown(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe828), modifier = modifier)
    @Composable
    fun caretLeft(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe829), modifier = modifier)
    @Composable
    fun caretRight(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe82a), modifier = modifier)
    @Composable
    fun dragHandle(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe82b), "Drag handle", modifier = modifier)
    @Composable
    fun deleteTrash(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe82c), "Delete", modifier = modifier)
    @Composable
    fun sendToWatchUnchecked(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe82d), modifier = modifier)
    @Composable
    fun appsCrate(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe82e), modifier = modifier)
    @Composable
    fun appsCrateBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe82f), modifier = modifier)
    @Composable
    fun checkForUpdatesBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe830), modifier = modifier)
    @Composable
    fun connectToWatchBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe831), modifier = modifier)
    @Composable
    fun customizeNavbar(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe832), modifier = modifier)
    @Composable
    fun customizeNavbarBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe833), modifier = modifier)
    @Composable
    fun devicesBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe834), modifier = modifier)
    @Composable
    fun disconnectFromWatchBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe835), modifier = modifier)
    @Composable
    fun discord(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe836), modifier = modifier)
    @Composable
    fun discordBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe837), modifier = modifier)
    @Composable
    fun lockerBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe838), modifier = modifier)
    @Composable
    fun location(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe839), modifier = modifier)
    @Composable
    fun locationBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe83a), modifier = modifier)
    @Composable
    fun musicBoombox(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe83b), modifier = modifier)
    @Composable
    fun musicBoomboxBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe83c), modifier = modifier)
    @Composable
    fun musicDisk(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe83d), modifier = modifier)
    @Composable
    fun musicDiskBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe83e), modifier = modifier)
    @Composable
    fun musicHeadphones(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe83f), modifier = modifier)
    @Composable
    fun musicHeadphonesBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe840), modifier = modifier)
    @Composable
    fun musicNote(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe841), modifier = modifier)
    @Composable
    fun musicNoteBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe842), modifier = modifier)
    @Composable
    fun musicRadio(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe843), modifier = modifier)
    @Composable
    fun musicRadioBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe844), modifier = modifier)
    @Composable
    fun musicTurntable(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe845), modifier = modifier)
    @Composable
    fun musicTurntableBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe846), modifier = modifier)
    @Composable
    fun notificationEmail(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe847), modifier = modifier)
    @Composable
    fun notificationEmailBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe848), modifier = modifier)
    @Composable
    fun notificationMailbox(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe849), modifier = modifier)
    @Composable
    fun notificationMailboxBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe84a), modifier = modifier)
    @Composable
    fun notificationMegaphone(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe84b), modifier = modifier)
    @Composable
    fun notificationMegaphoneBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe84c), modifier = modifier)
    @Composable
    fun rebbleShoppingBagOld(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe84d), modifier = modifier)
    @Composable
    fun rebbleShoppingBagOldBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe84e), modifier = modifier)
    @Composable
    fun rocketBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe84f), modifier = modifier)
    @Composable
    fun searchBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe850), modifier = modifier)
    @Composable
    fun settingsSliders(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe851), modifier = modifier)
    @Composable
    fun settingsSlidersBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe852), modifier = modifier)
    @Composable
    fun settingsBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe853), modifier = modifier)
    @Composable
    fun timelinePinBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe854), modifier = modifier)
    @Composable
    fun floppyDiskBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe855), modifier = modifier)
    @Composable
    fun floppyDiskHealthDatabaseBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe856), modifier = modifier)
    @Composable
    fun healthJournalBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe857), modifier = modifier)
    @Composable
    fun silencePhoneBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe858), modifier = modifier)
    @Composable
    fun vibratingWatchBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe859), modifier = modifier)
    @Composable
    fun healthHeartBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe85a), modifier = modifier)
    @Composable
    fun watchAppsBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe85b), modifier = modifier)
    @Composable
    fun watchFacesBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe85c), modifier = modifier)
    @Composable
    fun healthSleepBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe85d), modifier = modifier)
    @Composable
    fun healthStepsBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe85e), modifier = modifier)
    @Composable
    fun analyticsBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe85f), modifier = modifier)
    @Composable
    fun developerSettingsBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe860), modifier = modifier)
    @Composable
    fun smsMessagesBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe861), modifier = modifier)
    @Composable
    fun unpairFromWatchBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe862), modifier = modifier)
    @Composable
    fun applyUpdateBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe863), modifier = modifier)
    @Composable
    fun dictationMicrophoneBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe864), modifier = modifier)
    @Composable
    fun aboutAppBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe865), modifier = modifier)
    @Composable
    fun shareBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe866), modifier = modifier)
    @Composable
    fun developerConnectionConsoleBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe868), modifier = modifier)
    @Composable
    fun rebbleStoreBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe869), modifier = modifier)
    @Composable
    fun notificationBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe86a), modifier = modifier)
    @Composable
    fun unknownAppBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe86b), modifier = modifier)
    @Composable
    fun sendToWatchCheckedBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe86c), modifier = modifier)
    @Composable
    fun deleteTrashBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe86d), modifier = modifier)
    @Composable
    fun sendToWatchUncheckedBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe86e), modifier = modifier)
    @Composable
    fun rebbleAppstore50(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe86f), modifier = modifier)
    @Composable
    fun rebbleAppstore50Background(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe870), modifier = modifier)
    @Composable
    fun deadWatchGhost80(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe871), modifier = modifier)
    @Composable
    fun deadWatchGhost80Background(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe872), modifier = modifier)
    @Composable
    fun rebbleAppstoreBoxClosed80(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe873), modifier = modifier)
    @Composable
    fun rebbleAppstoreBoxClosed80Background(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe874), modifier = modifier)
    @Composable
    fun rebbleAppstore80(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe875), modifier = modifier)
    @Composable
    fun rebbleAppstore80Background(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe876), modifier = modifier)
    @Composable
    fun rocketLaunchpad80(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe877), modifier = modifier)
    @Composable
    fun rocketLaunchpad80Background(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe878), modifier = modifier)
    @Composable
    fun rocket80(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe879), modifier = modifier)
    @Composable
    fun rocket80Background(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe87a), modifier = modifier)
    @Composable
    fun timeline(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe87b), "Timeline", modifier = modifier)
    @Composable
    fun timelineBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe87c), modifier = modifier)
    @Composable
    fun twitter(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe87d), modifier = modifier)
    @Composable
    fun twitterBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe87e), modifier = modifier)
    @Composable
    fun warning(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe87f), modifier = modifier)
    @Composable
    fun warningBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe880), modifier = modifier)
    @Composable
    fun dropdown(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe881), modifier = modifier)
    @Composable
    fun permissions(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe882), "Permissions", modifier = modifier)
    @Composable
    fun permissionsBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe883), modifier = modifier)
    @Composable
    fun unpairPhone(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe884), modifier = modifier)
    @Composable
    fun unpairPhoneBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe885), modifier = modifier)
    @Composable
    fun screenshotCamera(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe886), modifier = modifier)
    @Composable
    fun screenshotCameraBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe887), modifier = modifier)
    @Composable
    fun weatherCloud(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe888), modifier = modifier)
    @Composable
    fun weatherCloudBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe889), modifier = modifier)
    @Composable
    fun genericFloppyDisk(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe88a), modifier = modifier)
    @Composable
    fun genericFloppyDiskBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe88b), modifier = modifier)
    @Composable
    fun calendar(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe88c), "Calendar", modifier = modifier)
    @Composable
    fun calendarBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe88d), modifier = modifier)
    @Composable
    fun reddit(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe88e), modifier = modifier)
    @Composable
    fun redditBackground(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe88f), modifier = modifier)
    @Composable
    fun heartFilled(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe890), modifier = modifier)
    @Composable
    fun heartEmpty(modifier:  Modifier = Modifier.width(24.dp)) = TextIcon(font(), Char(0xe891), modifier = modifier)
}