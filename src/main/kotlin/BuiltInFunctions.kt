data class BuiltInFunctionDefinition(val name: String, val arity: Int)

val builtIns = listOf<BuiltInFunctionDefinition>(
    BuiltInFunctionDefinition("int_to_string", 1),
    BuiltInFunctionDefinition("greater_than", 2)
)