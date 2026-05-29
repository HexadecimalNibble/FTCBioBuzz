package org.hexnibble.localcorelib.newStuff.Motor

sealed class MotorType(
  val RPM: Int,
  val internalGearReduction: Double,
) {
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_30 : MotorType(30, 188.61078)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_43 : MotorType(43, 139.13824)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_60 : MotorType(60, 99.50754)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_84 : MotorType(84, 71.16483)
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_117 : MotorType(117, 50.89497)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_223 : MotorType(223, 26.85124)
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_312 : MotorType(312, 19.20321)
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_435 : MotorType(435, 13.73356)
  // internal: (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_1150 : MotorType(1150, 5.18182)
  // internal: (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_1620 : MotorType(1620, 3.70588)
  // internal: 1.0
  object GOBILDA_BARE : MotorType(6000, 1.0)

  data class CUSTOM(
    val rpm: Int,
    val igr: Double,
  ) : MotorType(rpm, igr)

  fun getAchievableMaxTicksPerSecond(CPR: Int) = CPR * RPM

  /**
   * Get actual position of motor accounting for internal gear ratios from a raw motor position.
   */
  fun getPosition(rawValue: Double, CPR: Int, externalGearChange: Double): Double = rawValue / CPR / internalGearReduction * externalGearChange

  /**
   * Return the velocity of the motor in RPM
   */
  fun getVelocity(rawValue: Double, CPR: Int, externalGearChange: Double): Double = rawValue / CPR / internalGearReduction * 60 * externalGearChange
}