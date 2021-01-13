package org.learn.code;


//Function<String, Long> parseLong = str -> Long.parseLong(str);
//Map<Long, PartnerDishMapping> partnerDishMappingMap = dishMappings.stream().collect(
//        Collectors.toMap(item -> parseLong.apply(item.getSkuId()), Function.identity(), (oldValue, newValue) -> newValue));
//
//List<AuthUser> userList;
//Set<Long> alreadyAddedUserSet;
////userList.stream().filter(t -> t != null).filter();
//Set<Long> userIdSet = userList.stream().filter(u -> !alreadyAddedUserSet.contains(u.getId())).map(AuthUser::getId).collect(Collectors.toSet());