package io.github.triplec.analyzer.domain

/**
 * 설명:
 *
 * @author 서버개발 / g-dev-server@dreamus.io
 */
enum class SpikeDirection(val description: String) {

    UP("급등"),
    DOWN("급락"),
    UNCHANGED("변동 없음")
}