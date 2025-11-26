package codes.chrishorner.personasns

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.Easing


const val AnimationDurationScale = 1

val BetterEaseOutBack: Easing = Easing { fraction ->
  try {
    EaseOutBack.transform(fraction)
  } catch (e: IllegalArgumentException) {
    1f
  }
}