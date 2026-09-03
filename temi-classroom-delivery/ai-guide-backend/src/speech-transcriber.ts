import OpenAI, { toFile } from "openai";

export interface SpeechTranscript {
  text: string;
  model: string;
  warning?: string;
}

export interface SpeechTranscriber {
  transcribe(audio: Uint8Array, mediaType: string): Promise<SpeechTranscript>;
}

export interface PcmWavInfo {
  durationMs: number;
  sampleRate: number;
  channels: number;
  bitsPerSample: number;
  dataBytes: number;
}

export class OpenAiSpeechTranscriber implements SpeechTranscriber {
  private readonly client: OpenAI;

  constructor(
    apiKey: string,
    private readonly model: string,
    client?: OpenAI,
    timeoutMillis = 30_000,
  ) {
    this.client = client ?? new OpenAI({
      apiKey,
      timeout: timeoutMillis,
      maxRetries: 0,
    });
  }

  async transcribe(audio: Uint8Array, mediaType: string): Promise<SpeechTranscript> {
    try {
      const response = await this.client.audio.transcriptions.create({
        file: await toFile(audio, "temi-question.wav", { type: mediaType }),
        model: this.model,
        language: "ko",
        response_format: "json",
      });
      const text = response.text.trim();
      if (text.length === 0) throw new Error("음성 인식 결과가 비어 있습니다.");
      return { text, model: this.model };
    } catch (error) {
      throw new SpeechTranscriptionError(`${this.model} 음성 인식에 실패했습니다.`, error);
    }
  }
}

export class UnavailableSpeechTranscriber implements SpeechTranscriber {
  constructor(private readonly message = "음성 인식 API가 설정되지 않았습니다.") {}

  async transcribe(): Promise<SpeechTranscript> {
    throw new SpeechTranscriptionError(this.message);
  }
}

export class FallbackSpeechTranscriber implements SpeechTranscriber {
  constructor(
    private readonly primary: SpeechTranscriber,
    private readonly fallback: SpeechTranscriber,
    private readonly primaryLabel = "gpt-transcribe",
    private readonly fallbackLabel = "whisper-1",
  ) {}

  async transcribe(audio: Uint8Array, mediaType: string): Promise<SpeechTranscript> {
    try {
      return await this.primary.transcribe(audio, mediaType);
    } catch {
      try {
        const result = await this.fallback.transcribe(audio, mediaType);
        return {
          ...result,
          warning: joinWarnings(
            `${this.primaryLabel} 실패로 ${this.fallbackLabel}을 사용했습니다.`,
            result.warning,
          ),
        };
      } catch (fallbackError) {
        throw new SpeechTranscriptionError(
          `${this.primaryLabel}과 ${this.fallbackLabel} 음성 인식에 실패했습니다.`,
          fallbackError,
        );
      }
    }
  }
}

export class SpeechTranscriptionError extends Error {
  constructor(message: string, cause?: unknown) {
    super(message, cause === undefined ? undefined : { cause });
    this.name = "SpeechTranscriptionError";
  }
}

export function inspectPcmWav(
  audio: Uint8Array,
  maxDurationMs: number,
): PcmWavInfo {
  const wav = Buffer.from(audio.buffer, audio.byteOffset, audio.byteLength);
  if (wav.length < 44 || ascii(wav, 0, 4) !== "RIFF" || ascii(wav, 8, 12) !== "WAVE") {
    throw new Error("올바른 WAV 파일이 아닙니다.");
  }

  let offset = 12;
  let format: { sampleRate: number; channels: number; bitsPerSample: number; byteRate: number } |
    undefined;
  let dataBytes: number | undefined;
  while (offset + 8 <= wav.length) {
    const chunkId = ascii(wav, offset, offset + 4);
    const chunkSize = wav.readUInt32LE(offset + 4);
    const payloadOffset = offset + 8;
    const payloadEnd = payloadOffset + chunkSize;
    if (payloadEnd > wav.length) throw new Error("WAV 청크 길이가 올바르지 않습니다.");
    if (chunkId === "fmt ") {
      if (chunkSize < 16) throw new Error("WAV PCM 형식 정보가 부족합니다.");
      const audioFormat = wav.readUInt16LE(payloadOffset);
      const channels = wav.readUInt16LE(payloadOffset + 2);
      const sampleRate = wav.readUInt32LE(payloadOffset + 4);
      const byteRate = wav.readUInt32LE(payloadOffset + 8);
      const blockAlign = wav.readUInt16LE(payloadOffset + 12);
      const bitsPerSample = wav.readUInt16LE(payloadOffset + 14);
      if (
        audioFormat !== 1 || channels !== 1 || sampleRate !== 16_000 ||
        bitsPerSample !== 16 || blockAlign !== 2 || byteRate !== 32_000
      ) {
        throw new Error("WAV는 16 kHz 모노 16-bit PCM이어야 합니다.");
      }
      format = { sampleRate, channels, bitsPerSample, byteRate };
    } else if (chunkId === "data") {
      dataBytes = chunkSize;
    }
    offset = payloadEnd + (chunkSize % 2);
  }
  if (format === undefined || dataBytes === undefined || dataBytes === 0) {
    throw new Error("WAV PCM 데이터가 비어 있습니다.");
  }
  if (dataBytes % 2 !== 0) throw new Error("WAV PCM 데이터 길이가 올바르지 않습니다.");
  const durationMs = Math.ceil(dataBytes * 1_000 / format.byteRate);
  if (durationMs > maxDurationMs) {
    throw new Error(`음성은 최대 ${Math.floor(maxDurationMs / 1_000)}초까지만 처리할 수 있습니다.`);
  }
  return {
    durationMs,
    sampleRate: format.sampleRate,
    channels: format.channels,
    bitsPerSample: format.bitsPerSample,
    dataBytes,
  };
}

function ascii(buffer: Buffer, start: number, end: number): string {
  return buffer.toString("ascii", start, end);
}

function joinWarnings(primary: string, secondary: string | undefined): string {
  return secondary === undefined || secondary.trim().length === 0
    ? primary
    : `${primary} ${secondary.trim()}`;
}
