package io.rebble.cobble.shared.domain.common

enum class PebbleWatchModel(val protocolNumber: Int, val jsName: String) {
    ClassicBlack(1, "pebble_black"),
    ClassicWhite(2, "pebble_white"),
    ClassicRed(3, "pebble_red"),
    ClassicOrange(4, "pebble_orange"),
    ClassicPink(5, "pebble_pink"),
    ClassicSteelSilver(6, "pebble_steel_silver"),
    ClassicSteelGunmetal(7, "pebble_steel_gunmetal"),
    ClassicFlyBlue(8, "pebble_fly_blue"),
    ClassicFreshGreen(9, "pebble_fresh_green"),
    ClassicHotPink(10, "pebble_hot_pink"),
    TimeWhite(11, "pebble_time_white"),
    TimeBlack(12, "pebble_time_black"),
    TimeRed(13, "pebble_time_red"),
    TimeSteelSilver(14, "pebble_time_steel_silver"),
    TimeSteelGunmetal(15, "pebble_time_steel_black"),
    TimeSteelGold(16, "pebble_time_steel_gold"),
    TimeRoundSilver14(17, "pebble_time_round_silver"),
    TimeRoundBlack14(18, "pebble_time_round_black"),
    TimeRoundSilver20(19, "pebble_time_round_silver_20"),
    TimeRoundBlack20(20, "pebble_time_round_black_20"),
    TimeRoundRoseGold14(21, "pebble_time_round_rose_gold"),
    TimeRoundRainbowSilver14(22, "pebble_time_round_silver_rainbow"),
    TimeRoundRainbowBlack20(23, "pebble_time_round_black_rainbow"),
    Pebble2SEBlack(24, "pebble_2_se_black_charcoal"),
    Pebble2HRBlack(25, "pebble_2_hr_black_charcoal"),
    Pebble2SEWhite(26, "pebble_2_se_white_gray"),
    Pebble2HRLime(27, "pebble_2_hr_charcoal_sorbet_green"),
    Pebble2HRFlame(28, "pebble_2_hr_charcoal_red"),
    Pebble2HRWhite(29, "pebble_2_hr_white_gray"),
    Pebble2HRAqua(30, "pebble_2_hr_white_turquoise"),
    Time2Gunmetal(31, "pebble_time_2_black"),
    Time2Silver(32, "pebble_time_2_silver"),
    Time2Gold(33, "pebble_time_2_gold"),
    TimeRoundBlackSilverPolish20(34, "pebble_time_round_polished_silver"),
    TimeRoundBlackGoldPolish20(35, "pebble_time_round_polished_gold"),
    Unknown(-1, "unknown_unknown");

    companion object {
        fun fromProtocolNumber(protocolNumber: Int): PebbleWatchModel {
            return entries.firstOrNull { it.protocolNumber == protocolNumber } ?: Unknown
        }
    }
}