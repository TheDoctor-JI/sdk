package com.robotemi.sdk

enum class SttLanguage(val value: Int) {
    SYSTEM(0),
    EN_US(1),
    ZH_CN(2),
    JA_JP(3),
    KO_KR(4),
    ZH_HK(5),
    ZH_TW(6),
    DE_DE(7),
    TH_TH(8),
    IN_ID(9),
    PT_BR(10),
    AR_EG(11),
    FR_CA(12),
    FR_FR(13),
    ES_ES(14),
    CA_ES(15),
    IW_IL(16),
    IT_IT(17),
    ET_EE(18),
    TR_TR(19),
    HI_IN(20), // Supported from 133 version
    EN_IN(21), // Supported from 133 version
    MS_MY(22), // Supported from 134 version
    VI_VN(23), // Supported from 134 version
    RU_RU(24), // Supported from 134 version
    EL_GR(25), // Supported from 134 version
    AZ_AZ(26), // Supported from 136 version
    ;

    companion object {

        @JvmStatic
        fun valueToEnum(value: Int): SttLanguage {
            return when (value) {
                0 -> SYSTEM
                1 -> EN_US
                2 -> ZH_CN
                3 -> JA_JP
                4 -> KO_KR
                5 -> ZH_HK
                6 -> ZH_TW
                7 -> DE_DE
                8 -> TH_TH
                9 -> IN_ID
                10 -> PT_BR
                11 -> AR_EG
                12 -> FR_CA
                13 -> FR_FR
                14 -> ES_ES
                15 -> CA_ES
                16 -> IW_IL
                17 -> IT_IT
                18 -> ET_EE
                19 -> TR_TR
                20 -> HI_IN
                21 -> EN_IN
                22 -> MS_MY
                23 -> VI_VN
                24 -> RU_RU
                25 -> EL_GR
                26 -> AZ_AZ
                else -> SYSTEM
            }
        }
    }
}