package io.rebble.cobble.shared.data

data class EventAttendee(
    val name: String?,
    val email: String?,
    val role: Role?,
    val isOrganizer: Boolean = false,
    val isCurrentUser: Boolean = false,
    val attendanceStatus: AttendanceStatus?
) {
    enum class Role {
        None,
        Required,
        Optional,
        Resource
    }

    // These are only the android values, other platforms should map specific values to the closest match
    enum class AttendanceStatus {
        None,
        Accepted,
        Declined,
        Invited,
        Tentative
    }
}