package org.hexnibble.localcorelib.newStuff.Motor

sealed class MotorEncoder(
  val CPR: Int
) {
  object GOBILDA_INTERNAL : MotorEncoder(28)
  object REV_CORE_HEX_INTERNAL : MotorEncoder(4)
  object REV : MotorEncoder(8192)
  object GO_BILDA_ODOPOD : MotorEncoder(2000)

  data class CUSTOM(
    val cpr: Int
  ) : MotorEncoder(cpr)
}