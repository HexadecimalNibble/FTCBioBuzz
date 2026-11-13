package org.firstinspires.ftc.teamcode

import com.pedropathing.ivy.Command
import com.pedropathing.ivy.commands.Commands.waitMs
import dev.nextftc.units.Milliseconds
import dev.nextftc.units.measuretypes.Time

fun Command.withTimeout(timeout: Time): Command = this.raceWith(waitMs(timeout.into(Milliseconds)))