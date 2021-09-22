package org.learn.web.dao;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Table: dish_shop
 */
@Data
public class DishShop {

    public void buildData(int index) {
        DishShop dishShop = this;
        dishShop.setUuid(null);
        dishShop.setAliasName("1232");
        dishShop.setAliasShortName("xsddsdsds");
        dishShop.setBarcode("barcodeee");
        dishShop.setBoxQty(22);
        dishShop.setBrandIdenty(22234L);
        dishShop.setBrandDishId(13343L);
        dishShop.setBrandDishUuid("BrandDishUuid");
        dishShop.setClearStatus(2);
        dishShop.setCreatorId(23232L);
        dishShop.setCreatorName("fdfw2");
        dishShop.setCurrRemainTotal(1213L);
        dishShop.setDishCode("ddisih" + index);
        dishShop.setDishDesc("dedewc" + index);
        dishShop.setDefProperty(2);
        dishShop.setDishIncreaseUnit(BigDecimal.TEN.add(BigDecimal.valueOf(index)));
        dishShop.setDishNameIndex("dsdsds" + index);
        dishShop.setDishTypeId(3325L);
        dishShop.setDishQty(BigDecimal.ONE);
        dishShop.setEnabledFlag(1);
        dishShop.setHasStandard(1);
        dishShop.setIsChangePrice(1);
        dishShop.setIsDiscountAll(1);
        dishShop.setIsManual(1);
        dishShop.setIsOrder(0);
        dishShop.setIsSingle(1);
        dishShop.setIsSendOutside(0);
        dishShop.setMarketPrice(BigDecimal.valueOf(index));
        dishShop.setMaxNum(index);
        dishShop.setMinNum(-index);
        dishShop.setName("ddddd" + index);
        dishShop.setProductId(21212L);
        dishShop.setResidueTotal(BigDecimal.valueOf(index * 2));
        dishShop.setSaleTotal(BigDecimal.valueOf(index * 3));
        dishShop.setSaleTotalWechat(BigDecimal.valueOf(index * 1.5));
        dishShop.setSaleType(1);
        dishShop.setScene("dd");
        dishShop.setServerCreateTime(new Date());
        dishShop.setServerUpdateTime(new Date());

        dishShop.setShopIdenty(8034342L);
        dishShop.setShortName("das" + index);
        dishShop.setSkuKey("skey " + index);
        dishShop.setSort(1);
        dishShop.setSource(1);
        dishShop.setStatusFlag(1);
        dishShop.setStepNum(BigDecimal.valueOf(index * 3));
        dishShop.setType(1);
        dishShop.setUnitId(111L);
        dishShop.setUuid(UUID.randomUUID().toString().replaceAll("-", ""));
        dishShop.setValidTime(new Date());
        dishShop.setVideoUrl("long url si ds kdwdfe kfdsfmif id  " + index);
        dishShop.setWeight(BigDecimal.valueOf(index * 4));
        dishShop.setWmType(1);
        dishShop.setUnvalidTime(new Date());
    }





    /**
     * 自增id : 自增id
     *
     * Table:     dish_shop
     * Column:    id
     * Nullable:  false
     */
    private Long id;

    /**
     * 门店菜品uuid : 唯一标识
     *
     * Table:     dish_shop
     * Column:    uuid
     * Nullable:  false
     */
    private String uuid;

    /**
     * 品牌菜品id : 品牌菜品id
     *
     * Table:     dish_shop
     * Column:    brand_dish_id
     * Nullable:  false
     */
    private Long brandDishId;

    /**
     * 品牌菜品uuid : 品牌菜品uuid
     *
     * Table:     dish_shop
     * Column:    brand_dish_uuid
     * Nullable:  false
     */
    private String brandDishUuid;

    /**
     * 菜品类型id : 菜品类型id
     *
     * Table:     dish_shop
     * Column:    dish_type_id
     * Nullable:  true
     */
    private Long dishTypeId;

    /**
     * 菜品编码 : 菜品编码
     *
     * Table:     dish_shop
     * Column:    dish_code
     * Nullable:  true
     */
    private String dishCode;

    /**
     * 菜品类型 : 菜品种类 0:单菜 1:套餐 2:加料 3:实体卡
     *
     * Table:     dish_shop
     * Column:    type
     * Nullable:  false
     */
    private Integer type;

    /**
     * 菜品名称 : 菜品名称
     *
     * Table:     dish_shop
     * Column:    name
     * Nullable:  false
     */
    private String name;

    /**
     * 别名 : 别名
     *
     * Table:     dish_shop
     * Column:    alias_name
     * Nullable:  true
     */
    private String aliasName;

    /**
     * 短名称
     *
     * Table:     dish_shop
     * Column:    short_name
     * Nullable:  true
     */
    private String shortName;

    /**
     * 别名：短名称
     *
     * Table:     dish_shop
     * Column:    alias_short_name
     * Nullable:  true
     */
    private String aliasShortName;

    /**
     * 菜品名称索引(首字母) : 菜品名称索引(首字母)
     *
     * Table:     dish_shop
     * Column:    dish_name_index
     * Nullable:  true
     */
    private String dishNameIndex;

    /**
     * 条形码 : 条形码
     *
     * Table:     dish_shop
     * Column:    barcode
     * Nullable:  true
     */
    private String barcode;

    /**
     * 单位id : 单位id
     *
     * Table:     dish_shop
     * Column:    unit_id
     * Nullable:  true
     */
    private Long unitId;

    /**
     * 单位换算称重
     *
     * Table:     dish_shop
     * Column:    weight
     * Nullable:  true
     */
    private BigDecimal weight;

    /**
     * 原价 : 原价
     *
     * Table:     dish_shop
     * Column:    market_price
     * Nullable:  false
     */
    private BigDecimal marketPrice;

    /**
     * 排序 : 排序
     *
     * Table:     dish_shop
     * Column:    sort
     * Nullable:  false
     */
    private Integer sort;

    /**
     * 菜品描述 : 菜品描述
     *
     * Table:     dish_shop
     * Column:    dish_desc
     * Nullable:  true
     */
    private String dishDesc;

    /**
     * 视频地址 : 视频地址
     *
     * Table:     dish_shop
     * Column:    video_url
     * Nullable:  true
     */
    private String videoUrl;

    /**
     * 库存类型 : 1、预制商品2、现制商品3、外购商品4、原物料5、半成品
     *
     * Table:     dish_shop
     * Column:    wm_type
     * Nullable:  true
     */
    private Integer wmType;

    /**
     * 销售类型 : 1 称重销售
     *
     * Table:     dish_shop
     * Column:    sale_type
     * Nullable:  true
     */
    private Integer saleType;

    /**
     * 起卖份数 : 起卖份数
     *
     * Table:     dish_shop
     * Column:    dish_increase_unit
     * Nullable:  false
     */
    private BigDecimal dishIncreaseUnit;

    /**
     * 是否允许单点 : 1 允许 2不允许
     *
     * Table:     dish_shop
     * Column:    is_single
     * Nullable:  false
     */
    private Integer isSingle;

    /**
     * 是否允许整单打折 : 1 允许 2不允许
     *
     * Table:     dish_shop
     * Column:    is_discount_all
     * Nullable:  false
     */
    private Integer isDiscountAll;

    /**
     * 来源：1 on_mind，2 on_mobile
     *
     * Table:     dish_shop
     * Column:    source
     * Nullable:  false
     */
    private Integer source;

    /**
     * 是否允许变价: 1 允许 2不允许
     *
     * Table:     dish_shop
     * Column:    is_change_price
     * Nullable:  true
     */
    private Integer isChangePrice;

    /**
     * 是否允许外送 : 1 允许 2不允许
     *
     * Table:     dish_shop
     * Column:    is_send_outside
     * Nullable:  false
     */
    private Integer isSendOutside;

    /**
     * 是否允许堂食1.允许2.不允许
     *
     * Table:     dish_shop
     * Column:    is_order
     * Nullable:  false
     */
    private Integer isOrder;

    /**
     * 商品自定义属性：1.普通商品；2.自定义商品
     *
     * Table:     dish_shop
     * Column:    def_property
     * Nullable:  false
     */
    private Integer defProperty;

    /**
     * 增量设置
     *
     * Table:     dish_shop
     * Column:    step_num
     * Nullable:  false
     */
    private BigDecimal stepNum;

    /**
     * 适合人群（小） : 适合人群（小）
     *
     * Table:     dish_shop
     * Column:    min_num
     * Nullable:  false
     */
    private Integer minNum;

    /**
     * 适合人群（大） : 适合人群（大）
     *
     * Table:     dish_shop
     * Column:    max_num
     * Nullable:  false
     */
    private Integer maxNum;

    /**
     * 估清 : 1：在售 ，2：卖光
     *
     * Table:     dish_shop
     * Column:    clear_status
     * Nullable:  false
     */
    private Integer clearStatus;

    /**
     * Table:     dish_shop
     * Column:    is_manual
     * Nullable:  true
     */
    private Integer isManual;

    /**
     * 每日售卖总数 : 每日售卖总数
     *
     * Table:     dish_shop
     * Column:    sale_total
     * Nullable:  false
     */
    private BigDecimal saleTotal;

    /**
     * 剩余总数 : 剩余总数
     *
     * Table:     dish_shop
     * Column:    residue_total
     * Nullable:  false
     */
    private BigDecimal residueTotal;

    /**
     * 外卖可售数量 : 外卖可售数量
     *
     * Table:     dish_shop
     * Column:    sale_total_wechat
     * Nullable:  false
     */
    private BigDecimal saleTotalWechat;

    /**
     * 外卖剩余数量
     *
     * Table:     dish_shop
     * Column:    residue_total_wechat
     * Nullable:  false
     */
    private BigDecimal residueTotalWechat;

    /**
     * 时间段起始时间 : 时间段起始时间
     *
     * Table:     dish_shop
     * Column:    valid_time
     * Nullable:  false
     */
    private Date validTime;

    /**
     * 时间段截止时间 : 时间段截止时间
     *
     * Table:     dish_shop
     * Column:    unvalid_time
     * Nullable:  false
     */
    private Date unvalidTime;

    /**
     * 销售场景 : 商户终端、微信、自助点餐。（3位二进制组合，“1”为可售，“0”为不可售，如“110”表示“商户终端-可售、微信-可售、自助点餐-不可售”）
     *
     * Table:     dish_shop
     * Column:    scene
     * Nullable:  false
     */
    private String scene;

    /**
     * 商户id : 商户id
     *
     * Table:     dish_shop
     * Column:    shop_identy
     * Nullable:  false
     */
    private Long shopIdenty;

    /**
     * 品牌id : 品牌id
     *
     * Table:     dish_shop
     * Column:    brand_identy
     * Nullable:  false
     */
    private Long brandIdenty;

    /**
     * 启用停用标识 : 区别与StatusFlag，启用停用的作用是该数据是有效数据，但是被停用。 1:启用;2:停用
     *
     * Table:     dish_shop
     * Column:    enabled_flag
     * Nullable:  true
     */
    private Integer enabledFlag;

    /**
     * 商品唯一编号 : 商品唯一编号，用算法生成
     *
     * Table:     dish_shop
     * Column:    sku_key
     * Nullable:  true
     */
    private String skuKey;

    /**
     * spu id
     *
     * Table:     dish_shop
     * Column:    product_id
     * Nullable:  true
     */
    private Long productId;

    /**
     * 是否有规格 : 是否有规格 1 是  2  不是
     *
     * Table:     dish_shop
     * Column:    has_standard
     * Nullable:  false
     */
    private Integer hasStandard;

    /**
     * 商品数量
     *
     * Table:     dish_shop
     * Column:    dish_qty
     * Nullable:  false
     */
    private BigDecimal dishQty;

    /**
     * 餐盒数量
     *
     * Table:     dish_shop
     * Column:    box_qty
     * Nullable:  false
     */
    private Integer boxQty;

    /**
     * 状态标识 : 状态标识 1:启用 2:禁用
     *
     * Table:     dish_shop
     * Column:    status_flag
     * Nullable:  false
     */
    private Integer statusFlag;

    /**
     * 创建者名称 : 创建者名称
     *
     * Table:     dish_shop
     * Column:    creator_name
     * Nullable:  true
     */
    private String creatorName;

    /**
     * 创建者id : 创建者id
     *
     * Table:     dish_shop
     * Column:    creator_id
     * Nullable:  true
     */
    private Long creatorId;

    /**
     * 最后修改者姓名 : 最后修改者姓名
     *
     * Table:     dish_shop
     * Column:    updator_name
     * Nullable:  true
     */
    private String updatorName;

    /**
     * 更新者id : 更新者id
     *
     * Table:     dish_shop
     * Column:    updator_id
     * Nullable:  true
     */
    private Long updatorId;

    /**
     * 服务器创建时间 : 服务器创建时间
     *
     * Table:     dish_shop
     * Column:    server_create_time
     * Nullable:  false
     */
    private Date serverCreateTime;

    /**
     * 服务器更新时间
     *
     * Table:     dish_shop
     * Column:    server_update_time
     * Nullable:  false
     */
    private Date serverUpdateTime;

    /**
     * 当前可销售数量，NULL代表没有限制
     *
     * Table:     dish_shop
     * Column:    curr_remain_total
     * Nullable:  true
     */
    private Long currRemainTotal;

}