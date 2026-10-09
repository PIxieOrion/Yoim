#version 330

uniform sampler2D InSampler;

layout(std140) uniform OutlineConfig {
    int RenderMode;
    float FillOpacity;
};

in vec2 texCoord;
out vec4 fragColor;

float quad(float value) {
    return value * value;
}

void main() {
    vec2 oneTexel = 1.0 / vec2(textureSize(InSampler, 0));
    vec4 current = texture(InSampler, texCoord);

    if (current.a > 0.001) {
        if (RenderMode == 1) discard;
        fragColor = vec4(current.rgb, current.a * FillOpacity);
        return;
    }

    if (RenderMode == 0) discard;

    float alpha = 0.0;
    vec4 edgeColor = vec4(0.0);
    for (int x = -3; x <= 3; x++) {
        for (int y = -3; y <= 3; y++) {
            if (x == 0 && y == 0) continue;
            vec4 sampleColor = texture(InSampler, texCoord + vec2(float(x), float(y)) * oneTexel);
            if (sampleColor.a > 0.001) {
                edgeColor = sampleColor;
                alpha += max(0.0, (3.0 - distance(vec2(float(x), float(y)), vec2(0.0))) / 5.0);
            }
        }
    }

    if (alpha <= 0.0) discard;
    fragColor = vec4(edgeColor.rgb, edgeColor.a * min(1.0, quad(alpha)));
}
