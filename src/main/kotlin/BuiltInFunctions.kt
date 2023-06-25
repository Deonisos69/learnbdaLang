data class BuiltInFunctionDefinition(val name: String, val arity: Int, val type: Type)

val builtIns = listOf<BuiltInFunctionDefinition>(
    BuiltInFunctionDefinition("int_to_string", 1, parseType("Int -> Text")),
    BuiltInFunctionDefinition("greater_than", 2, parseType("Int -> Int -> Bool"))
)