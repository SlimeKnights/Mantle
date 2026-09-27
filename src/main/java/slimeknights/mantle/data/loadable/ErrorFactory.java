package slimeknights.mantle.data.loadable;

import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import slimeknights.mantle.data.loadable.field.ConstantField;
import slimeknights.mantle.data.loadable.field.RecordField;

import java.util.function.Consumer;
import java.util.function.Function;

/** Simple helpers to create exceptions */
public interface ErrorFactory extends Consumer<String>, Function<String,RuntimeException> {
  /** Error factory for a json syntax error during parsing */
  ErrorFactory JSON_SYNTAX_ERROR = JsonSyntaxException::new;
  /** Error factory for a decoder exception */
  ErrorFactory DECODER_EXCEPTION = DecoderException::new;
  /** Error factory for a decoder exception */
  ErrorFactory ENCODER_EXCEPTION = EncoderException::new;
  /** Error factory for a json during writing JSON */
  ErrorFactory RUNTIME = new ErrorFactory() {
    @Override
    public RuntimeException create(String error) {
      return new RuntimeException(error);
    }

    @Override
    public RuntimeException create(RuntimeException base) {
      return base;
    }
  };
  /** Field for constructors wishing to possibly throw */
  RecordField<ErrorFactory,Object> FIELD = new ConstantField<>(JSON_SYNTAX_ERROR, DECODER_EXCEPTION);

  /** Throws an exception from the given error */
  @Override
  default void accept(String error) {
    throw create(error);
  }

  /** Creates an exception with a string error */
  RuntimeException create(String error);

  /** Alias for {@link #create(String)} to adhere to {@link Function} */
  @Override
  default RuntimeException apply(String error) {
    return create(error);
  }

  /** Creates an exception wrapping the given exception message */
  default RuntimeException create(RuntimeException base) {
    return create(base.getMessage());
  }
}
