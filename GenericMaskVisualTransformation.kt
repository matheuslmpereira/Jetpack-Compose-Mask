import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview

class GenericMaskVisualTransformation(
    private val mask: String,
    private val maskSlotSignal: Char = '#'
) : VisualTransformation {

    private val offsetMapping = getOffsetMapping(mask)

    override fun filter(text: AnnotatedString): TransformedText {
        val unmasked = text.text

        if (unmasked.isEmpty()) return TransformedText(AnnotatedString(unmasked), OffsetMapping.Identity)

        var textCharIndex = 0
        var maskedString = ""

        for (maskChar in mask) {
            if (maskChar != maskSlotSignal) {
                maskedString += maskChar
            } else {
                if (textCharIndex >= unmasked.length) break
                maskedString += unmasked[textCharIndex]
                textCharIndex++
            }
        }

        return TransformedText(AnnotatedString(maskedString), offsetMapping)
    }

    private fun getOffsetMapping(mask: String) = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return offset + offsetMaskCount(offset)
        }

        override fun transformedToOriginal(offset: Int): Int {
            return offset - offsetMaskCount(offset)
        }

        private fun offsetMaskCount(offset: Int): Int {
            var maskOffsetCount = 0
            var dataCount = 0

            for (maskChar in mask) {
                if (maskChar != maskSlotSignal) maskOffsetCount++
                else if (++dataCount > offset) break
            }

            return maskOffsetCount
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
fun previewGenericMaskVisualTransformation() {
    val testMask = "##/##/####"
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val dateSize = testMask.count { char -> char == '#' }

    TextField(
        value = text,
        onValueChange = {
            if (it.text.length <= dateSize) text = it
        },
        placeholder = @Composable {
            Text(text = testMask)
        },
        visualTransformation = GenericMaskVisualTransformation(testMask),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
