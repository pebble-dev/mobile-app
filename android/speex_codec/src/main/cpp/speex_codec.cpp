#include <jni.h>
#include <string>
#include <speex/speex.h>

static jfieldID speexDecBits;
static jfieldID speexDecState;

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_speex_1codec_SpeexCodec_decode(JNIEnv *env, jobject thiz,
                                                jbyteArray encoded_frame, jobject out_frame, jboolean has_header_byte) {
    jbyte *encoded_frame_data = env->GetByteArrayElements(encoded_frame, nullptr);
    jsize encoded_frame_length = env->GetArrayLength(encoded_frame);
    auto *out_frame_data = reinterpret_cast<spx_int16_t *>(env->GetDirectBufferAddress(out_frame));
    auto *bits = reinterpret_cast<SpeexBits *>(env->GetLongField(thiz, speexDecBits));
    auto *dec_state = reinterpret_cast<void *>(env->GetLongField(thiz, speexDecState));
    int offset = has_header_byte ? 1 : 0;
    speex_bits_read_from(bits, reinterpret_cast<char *>(encoded_frame_data)+offset, encoded_frame_length-offset);
    int result = speex_decode_int(dec_state, bits, out_frame_data);
    env->ReleaseByteArrayElements(encoded_frame, encoded_frame_data, 0);
    return result;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_speex_1codec_SpeexCodec_initSpeexBits(JNIEnv *env, jobject thiz) {
    auto *bits = new SpeexBits;
    speex_bits_init(bits);
    return reinterpret_cast<jlong>(bits);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_speex_1codec_SpeexCodec_initDecState(JNIEnv *env, jobject thiz, jlong sample_rate,
                                                      jint bit_rate) {
    auto *dec_state = speex_decoder_init(&speex_wb_mode);
    speex_decoder_ctl(dec_state, SPEEX_SET_SAMPLING_RATE, &sample_rate);
    return reinterpret_cast<jlong>(dec_state);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_speex_1codec_SpeexCodec_destroySpeexBits(JNIEnv *env, jobject thiz,
                                                          jlong speex_bits) {
    auto *bits = reinterpret_cast<SpeexBits *>(speex_bits);
    delete bits;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_speex_1codec_SpeexCodec_destroyDecState(JNIEnv *env, jobject thiz,
                                                         jlong dec_state) {
    auto *state = reinterpret_cast<void *>(dec_state);
    speex_decoder_destroy(state);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_speex_1codec_SpeexCodec_initNative(JNIEnv *env, jobject thiz) {
    jclass clazz = env->GetObjectClass(thiz);
    speexDecBits = env->GetFieldID(clazz, "speexDecBits", "J");
    speexDecState = env->GetFieldID(clazz, "speexDecState", "J");
}