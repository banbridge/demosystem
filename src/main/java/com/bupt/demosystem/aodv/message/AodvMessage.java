package com.bupt.demosystem.aodv.message;

import com.bupt.demosystem.aodv.message.tool.AodvMessageType;

import java.io.*;
import java.net.InetSocketAddress;
import java.time.LocalTime;

/**
 * @author banbridge
 * 所有消息的父亲节点
 */
public class AodvMessage implements Serializable {

    /**
     * 1.RREQ节点向其邻居节点广播RREQ报文用于路由发现，（Hello报文相当于TTL为1的RREP报文）
     * 2.RREP当找到目的节点或某中间节点路由表含有到目的节点的路由记录时，节点向源节点发送RREP报文
     * 3.RERR邻居节点间周期性的互相广播“Hello”报文，用来保持联系，若在一段时间内没有收到“Hello”报文，
     * 则认为链路断开，然后节点产生RERR（路由错误报文）报文向源节点报告情况
     */
    private AodvMessageType packetType;

    /**
     * 模拟ip数据包中的TTL
     */
    private int TTL;

    /**
     * 每个消息的产生的时间戳
     */
    private LocalTime createTime;

    /**
     * 模拟ip协议
     * 上一跳和下一跳地址
     */
    private InetSocketAddress lastHopAddress;
    private InetSocketAddress nextHopAddress;

    /**
     * 模拟ip协议
     * 源地址和目的地址
     */
    private InetSocketAddress originAddress;
    private InetSocketAddress dstAddress;


    private Object object;

    public AodvMessage(AodvMessageType packetType, Object msg) {
        this.packetType = packetType;
        this.object = msg;
    }

    public AodvMessage() {

    }

    public InetSocketAddress getLastHopAddress() {
        return lastHopAddress;
    }

    public void setLastHopAddress(InetSocketAddress lastHopAddress) {
        this.lastHopAddress = lastHopAddress;
    }

    public InetSocketAddress getNextHopAddress() {
        return nextHopAddress;
    }

    public void setNextHopAddress(InetSocketAddress nextHopAddress) {
        this.nextHopAddress = nextHopAddress;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public AodvMessageType getPacketType() {
        return packetType;
    }

    public void setPacketType(AodvMessageType packetType) {
        this.packetType = packetType;
    }

    public void subTTL() {
        this.TTL--;
    }

    public LocalTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalTime createTime) {
        this.createTime = createTime;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public InetSocketAddress getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(InetSocketAddress originAddress) {
        this.originAddress = originAddress;
    }

    public InetSocketAddress getDstAddress() {
        return dstAddress;
    }

    public void setDstAddress(InetSocketAddress dstAddress) {
        this.dstAddress = dstAddress;
    }

    /**
     * 字节流和对象互相转化
     *
     * @param bytes bytes为要转化的字节流
     */
    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    public static byte[] objectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

    /*
      - 在节点发起路由发现之前，它必须增加它自己的序列号。这可以防止与
      先前建立的通往发起者的反向路由请求。
      - 紧接在目的地节点发起 RREP 之前响应一个 RREQ，它必须更新它自己的序列号到
      其当前序列号和目的地的最大值RREQ 数据包中的序列号。
     */


    /*
        6.1 维护序列号
        6.2 路由表条目和前体列表
     */

    /*
      为了减少网络拥塞，源节点的重复尝试 在单个目的地的路由发现必须使用二进制
      指数退避。源节点第一次广播 RREQ，它等待 NET_TRAVERSAL_TIME 毫秒以接收 RREP。
      如果在该时间内未收到 RREP，则源节点发送一个新的 RREQ。在计算之后等待 RREP 的时间时
      发送第二个 RREQ，源节点必须使用二进制指数退避。因此，RREP 的等待时间
      第二个RREQ对应的是2 * NET_TRAVERSAL_TIME毫秒。如果在这段时间内没有收到 RREP，
      可以发送另一个 RREQ，最多 RREQ_RETRIES 次额外尝试在第一个 RREQ 之后。对于每一次额外的尝试，等待时间
      因为 RREP 乘以 2，所以时间符合二元指数退避。
     */


    /*
      为了防止 RREQ 在网络范围内不必要的传播，发起节点应该使用扩展环搜索技术。在
      扩展环搜索，始发节点最初使用 TTL = RREQ 数据包 IP 标头中的 TTL_START 并设置超时
      接收到 RING_TRAVERSAL_TIME 毫秒的 RREP。RING_TRAVERSAL_TIME 的计算方法如第 10 节所述。这
      用于计算 RING_TRAVERSAL_TIME 的 TTL_VALUE 设置为等于IP 报头中 TTL 字段的值。如果 RREQ 超时
      没有相应的 RREP，发起者广播 RREQ再次以 TTL_INCREMENT 递增 TTL。这继续
      直到 RREQ 中设置的 TTL 达到 TTL_THRESHOLD，超过该值TTL = NET_DIAMETER 用于每次尝试。每次超时
      接收 RREP 的时间是 RING_TRAVERSAL_TIME。当需要让所有重试遍历整个 ad hoc 网络，这可以是
      通过将 TTL_START 和 TTL_INCREMENT 都配置为与 NET_DIAMETER 相同的值。
     */

    /*
           当一个节点收到一个 RREQ 时，它首先创建或更新一个路由到没有有效序列号的前一跳（参见第 6.2 节）
           然后检查以确定它是否收到了相同的 RREQ发起者 IP 地址和 RREQ ID 至少在最后一个
           PATH_DISCOVERY_TIME。如果已经收到这样的 RREQ，则节点默默地丢弃新收到的 RREQ。
           >首先，它首先将 RREQ 中的跳跃计数值加一，以考虑通过中间节点的新跃点。然后节点
           搜索到发起方 IP 地址的反向路由（请参阅第 6.2 节），使用最长前缀匹配。如果需要，路线
           使用来自源的创建者序列号创建或更新RREQ 在其路由表中。如果节点收到返回给发起 RREQ 的节点的 RREP
          （由发起方 IP 地址标识）。当反向路由是创建或更新，路由上的以下操作也执行：
          1. 将来自 RREQ 的发起方序列号与路由表条目中对应的目标序列号
             如果大于现有值，则复制
          2.有效序列号字段设置为true；
          3. 路由表中的下一跳成为节点收到 RREQ（从源 IP 地址中获取）
             IP 标头，通常不等于发起方 IP 地址RREQ 消息中的字段）；
          4、从RREQ消息中的Hop Count中复制hop count；

          每当收到 RREQ 消息时，反向的 Lifetime发起方 IP 地址的路由条目设置为最大值
          (ExistingLifetime, MinimalLifetime)，其中MinimalLifetime = (当前时间 + 2*NET_TRAVERSAL_TIME -
                                     2*HopCount*NODE_TRAVERSAL_TIME）。
          当前节点可以使用反向路由转发数据包与路由表中的任何其他路由相同。

          如果一个节点没有生成 RREP（按照第 6.6 节），如果传入的 IP 标头的 TTL 大于 1，
          节点更新并广播 RREQ 到地址 255.255.255.255在其配置的每个接口上（参见第 6.14 节）。更新
          RREQ，出站 IP 报头中的 TTL 或跳数限制字段是减一，RREQ 消息中的 Hop Count 字段为
          增加 1，以考虑通过新的跃点中间节点。最后，目标序列号请求的目的地设置为相应的最大值在 RREQ 消息中收到的值，以及目标序列
          节点当前为请求的目的地维护的值。但是，转发节点不得修改其维护的值目标序列号，即使在传入的 RREQ 大于当前由
          转发节点。

          否则，如果节点确实生成了 RREP，则该节点丢弃请求。请注意，如果中间节点回复每个传输
          对于特定目的地的 RREQ，结果可能是目的地不会收到任何发现消息。在这在这种情况下，目的地没有获知到目的地的路线
          来自 RREQ 消息的始发节点。这可能会导致目的地启动路由发现（例如，如果
          发起者正在尝试建立 TCP 会话）。为了使目的地学习到始发节点的路由，发起节点应该在
          RREQ 如果由于任何原因目的地可能需要一条路线始发节点。如果响应带有“G”标志的 RREQ
          设置，一个中间节点返回一个 RREP，它也必须单播一个免费 RREP 到目标节点（参见第 6.6.3 节）。
     */

    /**
     * 6.6 . 生成路由回复
     * 在以下情况下，节点会生成 RREP：
     * (i) 它本身就是目的地，或
     * (ii) 它有一条到达目的地的活动路线，目的地节点现有路由表条目中的序列号
     * 因为目的地是有效的并且大于或等于RREQ 的目标序列号（比较使用有符号的 32 位算术）和“仅目的地”
     * ('D') 标志未设置。
     */

    /*
          6.6.1 . 目的地生成路由回复

          如果生成节点是目的地本身，它必须递增如果 RREQ 中的序列号，则将其自己的序列号加一
          数据包等于该增量值。否则，目标在生成之前不会更改其序列号
          RREP 消息。目标节点将其（可能是新的递增）序列号到目标序列号
          RREP 字段，并在 Hop Count 字段中输入值零的 RREP。

          目标节点将值 MY_ROUTE_TIMEOUT（参见第10节）复制到 RREP 的 Lifetime 字段中。每个节点可以重新配置
          它的 MY_ROUTE_TIMEOUT 值，在轻度约束内（参见第10节）。
     */

    /*
         6.6.2 . 中间节点生成路由回复

         如果生成 RREP 的节点不是目的节点，而是取而代之的是沿从发起者到的路径的中间跳跃
         目的地，它复制其已知的序列号目的地到 RREP 中的目的地序列号字段信息。

         中间节点通过放置最后一跳节点（从它接收到 RREQ，如IP 标头中的源 IP 地址字段）到前体列表中
         前向路由条目——即目标 IP 的条目地址。中间节点也更新它的路由表条目对于发起 RREQ 的节点，将下一跳指向
         反向路由条目的前体列表中的目的地——即，RREQ 的发起方 IP 地址字段的条目消息数据。

         中间节点将其距离以跳数表示 目的地（由路由表中的跳数表示） CountRREP 中的字段。RREP 的 Lifetime 字段由下式计算
         从其路由的到期时间中减去当前时间表项。
     */

    /*
        6.7 . 接收和转发路由回复

        当一个节点收到一个 RREP 消息时，它会搜索（使用最长的前缀匹配）用于到前一跳的路由。如果需要，路线
        为前一跳创建，但没有有效的序列号（见第 6.2 节）。接下来，节点然后增加跳数
        RREP 中的值加一，以说明通过中间节点。将此增量值称为“新跳数”。
        如果没有，则创建此目的地的前向路由已经存在。否则，节点比较 Destination Sequence
        带有自己存储的目标序列号的消息中的编号对于 RREP 消息中的目标 IP 地址。经比较，
        现有条目仅在以下情况下更新：

        (i) 路由表中的序号标记为 在路由表条目中无效。

        (ii) RREP 中的 Destination Sequence Number 大于节点的目标序列号的副本和已知值有效，或

        (iii) 序列号相同，但路由是标记为不活动，或

        (iv) 序列号相同，New Hop Count 为小于路由表项中的跳数。

        如果创建或更新到目的地的路由表条目，
        然后发生以下操作：

        - 路线被标记为活动，

        - 目标序列号被标记为有效，

        - 路由条目中的下一跳被分配为来自收到RREP，由源IP指示IP 标头中的地址字段，

        - 跳数设置为新跳数的值，

        - 到期时间设置为当前时间加上
          RREP 消息中的生命周期，

        - 目标序列号是目标序列
          RREP 消息中的编号。

        当前节点可以随后使用此路由转发数据到目的地的数据包。

        如果当前节点不是Originator IP指示的节点RREP 消息中的地址和转发路由已创建或
        如上所述更新，节点查询其路由表条目用于发起节点确定 RREP 的下一跳
        包，然后使用该路由表条目中的信息。如果一个节点转发一个 RREP在可能有错误或单向的链接上，
        节点应该设置“A”标志以要求接收者RREP 通过发送 RREP-ACK 消息确认收到 RREP返回（见第 6.8 节）。

        当任何节点传输 RREP 时，该节点的前驱列表通过添加下一个目标节点来更新相应的目标节点
        RREP 转发到的跳节点。此外，在每个节点用于转发 RREP 的（反向）路由的生命周期更改为
        (existing-lifetime, (current time +ACTIVE_ROUTE_TIMEOUT）。最后，下一跳的前体列表
        向目的地更新以包含前往的下一跳来源。

     */

    /*
        6.8 . 单向链路上的操作
        RREP 传输可能会失败，特别是如果触发 RREP 的 RREQ 传输发生在单向
        关联。如果同一个路由发现没有产生其他 RREP尝试到达发起 RREQ 消息的节点，
        发起者将在超时后重新尝试路由发现（请参阅第 6.3 节）。然而，同样的场景很可能会重演
        没有任何改进，即使经过也不会发现任何路线反复重试。除非采取纠正措施，否则可能会发生这种情况
        即使在始发者和目的地之间的双向路由存在。使用 RREQ 广播传输的链路层将
        无法检测到此类单向链路的存在。在AODV，任何节点只作用于第一个具有相同 RREQ ID 和
        忽略任何后续的 RREQ。例如，假设第一个RREQ 沿着具有一个或多个单向的路径到达
        链接）。后续的 RREQ 可能通过双向路径到达假设存在这样的路径），但它将被忽略。

        为了防止这个问题，当一个节点检测到它的传输RREP 消息失败，它会记住失败的下一跳
        “黑名单”集中的 RREP。此类故障可以通过缺少链路层或网络层确认（例如，RREP-
        确认）。一个节点忽略从它的任何节点收到的所有 RREQ黑名单设置。节点从黑名单集合中删除后
        BLACKLIST_TIMEOUT 期间（参见第 10 节）。应该设置这个时间段到执行允许数量所需的时间上限
        路由请求重试尝试的次数，如第 6.3 节所述。

        请注意，RREP-ACK 数据包不包含任何关于它正在确认哪个 RREP。RREP-ACK 的时间
        收到的可能会在发送 RREP 之后 带有“A”位。预计这些信息足以向 RREP 的发件人保证该链接是
        目前是双向的，没有任何真正的依赖特定的 RREP 消息被确认。然而，这种保证
        通常不能指望永久有效。

        6.9 . Hello 消息

        节点可以通过广播本地 Hello 来提供连接信息消息。一个节点应该只使用 hello 消息，如果它是一个
        活动路线。每 HELLO_INTERVAL 毫秒，节点检查它是否发送了广播（例如，RREQ 或适当的层
        2 条消息）在最后一个 HELLO_INTERVAL 中。如果没有，它可以广播一个 TTL = 1 的 RREP，称为 Hello 消息，带有 RREP
        消息字段设置如下：
         目标 IP 地址 节点的 IP 地址。
        `目标序列号 节点的最新序列号。
        `跳数 0
         终生 ALLOWED_HELLO_LOSS * HELLO_INTERVAL

        一个节点可以通过侦听来自它的数据包来确定连通性一组邻居。如果在过去的 DELETE_PERIOD 内，它收到了
        来自邻居的 Hello 消息，然后对于该邻居没有接收任何数据包（Hello 消息或其他）超过

        ALLOWED_HELLO_LOSS * HELLO_INTERVAL 毫秒，节点应该假设到这个邻居的链接当前丢失。当这
        发生时，节点应该按照第 6.11 节进行处理。

        每当一个节点收到来自邻居的 Hello 消息时，该节点应该确保它有一条到邻居的活动路由，并且
        如有必要，创建一个。如果路由已经存在，则如有必要，应增加路线的使用寿命，以达到
        至少 ALLOWED_HELLO_LOSS * HELLO_INTERVAL。去往的路线邻居，如果存在，必须随后包含最新的
        来自 Hello 消息的目标序列号。当前节点现在可以开始使用这条路由转发数据包了。路线
        由 hello 消息创建，不被任何其他活动路由使用将有空的前体列表并且不会触发 RERR 消息
        如果邻居搬走并且邻居超时发生。

        6.10 . 维护本地连接

        每个转发节点应该跟踪它的持续连接到其活动的下一跳（即，哪些下一跳或前驱具有
        上次转发节点转发数据包ACTIVE_ROUTE_TIMEOUT)，以及已经传输的邻居
        最后一次的问候消息 (ALLOWED_HELLO_LOSS * HELLO_INTERVAL)。一个节点可以维护关于其持续的准确信息
        连接到这些活动的下一跳，使用一个或多个可用的链路或网络层机制，如下所述。

        - 任何合适的链路层通知，例如由IEEE 802.11，可用于确定连接性，每次
          数据包被传输到活动的下一跳。例如，缺席发送 RTS 后链路层 ACK 或未能获得 CTS，
          即使在重传尝试达到最大次数后，表示到此活动下一跳的链路丢失。

        - 如果第 2 层通知不可用，则被动确认应该在下一跳转发时使用
          数据包，通过侦听通道进行传输尝试由下一跳制造。如果没有检测到传输
          NEXT_HOP_WAIT 毫秒或下一跳是目的地（和因此不应转发数据包）以下之一
          应该使用方法来确定连通性：
            * 接收来自下一个的任何数据包（包括 Hello 消息）跳。
            * RREQ 单播到下一跳，请求到下一跳的路由跳。
            * ICMP Echo Request 消息单播到下一跳。

        如果这些方法中的任何一种都无法检测到下一跳的链接，
        转发节点应该假设链路丢失，并采取按照第 6.11 节中规定的方法采取纠正措施。

        6.11 . 路由错误 (RERR) 消息、路由到期和路由删除
        通常，路由错误和断链处理需要 以下步骤：
        - 使现有路线无效
        - 列出受影响的目的地
        - 确定哪些邻居（如果有）可能会受到影响
        - 向此类邻居提供适当的 RERR

        路由错误（RERR）消息可以是广播的（如果有许多前体），单播（如果只有 1 个前体），或
        迭代单播到所有前体（如果广播是不当）。即使当 RERR 消息以迭代方式单播到
        几个前体，它被认为是一个单一的控制消息 用于下文中的描述。和
        这种理解，一个节点不应该产生超过每秒 RERR_RATELIMIT RERR 消息。

        节点在三种情况下发起对 RERR 消息的处理：

        (i) 如果它检测到活动节点下一跳的链路中断传输数据时在其路由表中路由（和
        路由修复，如果尝试，不成功），或

        (ii) 如果它得到一个数据包，它的目的地是一个节点，它没有活动路线并且没有修复（如果
        使用本地维修），或

        (iii) 如果它从邻居那里收到一个或多个 RERR 活动路线。

        对于情况（i），节点首先列出不可达目的地的列表由无法到达的邻居和任何额外的本地路由表中的
        目的地（或子网，参见第 7 节）使用不可达邻居作为下一跳。在这种情况下，如果
        发现子网路由新近不可达，IP 目的地子网的地址是通过将零附加到子网前缀，如路由表条目中所示。这是
        明确的，因为已知前体有路由表该子网具有兼容前缀长度的信息。

        对于情况（ii），只有一个无法到达的目的地，即无法传递的数据包的目的地。为了
        情况 (iii)，列表应包含 RERR 中的目的地在本地路由中存在对应的条目
        将接收到的 RERR 的发送器作为下一跳的表。

        列表中的一些无法到达的目的地可以被使用 相邻节点，因此可能需要发送一个（新的）
        恢复。RERR 应该包含那些目的地创建的不可达目的地列表并且有一个非空
        前体清单。

        应该接收 RERR 的相邻节点都是那些属于至少一个不可到达的前体列表新创建的 RERR 中的目的地。如果只有一个
        需要接收 RERR 的唯一邻居，RERR 应该是向那个邻居单播。否则通常会发送 RERR
        到本地广播地址（目标 IP == 255.255.255.255，TTL == 1) 无法到达的目的地，以及它们对应的
        目标序列号，包含在数据包中。目标计数RERR 报文字段表示不可达数
        数据包中包含的目的地。

        就在传输 RERR 之前，在可能影响目标序列号的路由表无法到达的目的地。对于这些目的地中的每一个，
        相应的路由表条目更新如下：

        1.这个路由条目的目的序列号，如果它存在且有效，在上述情况 (i) 和 (ii) 中递增，
        并从上述情况 (iii) 中的传入 RERR 中复制。

        2.通过将路由条目标记为无效来使条目无效

        3. Lifetime 字段更新为当前时间加上 DELETE_PERIOD。在此之前，不应删除该条目。

        请注意，路由表中的 Lifetime 字段起着双重作用——对于活动路线，它是到期时间，对于无效路线
        现在是删除时间。如果收到无效的数据包路线，Lifetime 字段更新为当前时间加上
        DELETE_PERIOD。DELETE_PERIOD 的确定在第 10 节。

        6.12 . 本地维修

        当活动路由中发生链路中断时，其上游节点中断可以选择在本地修复链接，如果目的地
        距离不超过 MAX_REPAIR_TTL 跳。修复链接中断，节点增加目的地的序列号
        然后为该目的地广播一个 RREQ。RREQ 的 TTL最初应设置为以下值：
             MAX(MIN_REPAIR_TTL，0.5 * #hops）+ LOCAL_ADD_TTL，
        其中#hops 是到发送方（发起方）的跳数当前无法投递的数据包。因此，本地修复尝试将
        通常对发起节点不可见，并且总是有 TTL>= MIN_REPAIR_TTL + LOCAL_ADD_TTL。启动修复的节点
        然后等待发现周期接收 RREP 以响应 请求。在本地修复期间，数据包应该被缓冲。如果，在
        发现期结束，修复节点还没有收到RREP（或其他创建或更新路由的控制消息）用于
        该目的地，它按照第 6.11 节中的描述进行为该目的地发送 RERR 消息。

        另一方面，如果节点收到一个或多个 RREP（或其他控制消息创建或更新到所需的路由
        目的地）在发现期间，它首先比较跳具有跳数字段中的值的新路由的计数
        该目的地的路由表条目无效。如果跳数为新确定的到达目的地的路由大于跳数
        节点应该发出 RERR 的先前已知路由的计数目的地的消息，设置了“N”位。然后它继续
        如第 6.7 节所述，更新其路由表条目目的地。

        收到带有“N”标志设置的 RERR 消息的节点不得删除到该目的地的路线。采取的唯一行动应该是
        是消息的重传，如果 RERR 从沿着那条路线的下一跳，如果有一个或多个前兆
        该路由到目的地的节点。当发起节点接收带有“N”标志设置的 RERR 消息，如果此消息来
        从沿其路由的下一跳到目的地，然后始发节点可以选择重新启动路由发现，因为在第 6.3 节中描述。

        局部修复路由中的链路中断有时会导致增加到这些目的地的路径长度。在本地修复链接是
        可能会增加能够传输的数据包的数量传送到目的地，因为数据包不会被丢弃 当 RERR 传播到始发节点时。发送 RERR 到

        本地修复链路中断后的发起节点可能允许始发者找到一条更好的到达目的地的新路线，
        基于当前节点位置。然而，它不需要始发节点重建路由，作为始发者可能完成，
        或即将完成，数据会话。

        当链接沿活动路线断开时，通常会出现多个无法到达的目的地。上游的节点
        丢失的链接仅尝试立即进行本地修复数据包前往的目的地。其他
        使用相同链路的路由必须被标记为无效，但节点处理本地修复可以标记每个这样的新丢失的路线
        可本地修复；路由表中的这个本地修复标志必须是当路线超时时重置（例如，在路线没有被
        ACTIVE_ROUTE_TIMEOUT 处于活动状态）。在超时发生之前，当数据包到达时，将根据需要修复这些其他路由
        其他目的地。因此，这些路线会根据需要进行修复；如果数据包没有到达该路由，则该路由将
        无法修复。或者，根据当地的拥堵情况，节点可以开始建立本地修复的过程
        其他路由，无需等待新数据包到达。经过主动修复因丢失而中断的线路
        链接，这些路由的传入数据包将不受约束到修复路由的延迟，可以立即转发。
        但是，在收到数据包之前修复路由冒着修复不再使用的路线的风险。
        因此，取决于网络中的本地流量和是否正在经历拥塞，节点可以选择
        在收到数据包之前主动修复路由；否则，它可以等到接收到一个数据，然后开始
        路线的修复。

        6.13 . 重启后的动作

        参与自组织网络的节点必须采取某些行动 重新启动后，因为它可能会丢失所有序列号记录
        目的地，包括它自己的序列号。然而，可能有是使用此节点作为活动下一跳的相邻节点。
        这可能会产生路由循环。为了防止这种情况可能性，重启时的每个节点在等待 DELETE_PERIOD 之前
        传输任何路由发现消息。如果节点收到RREQ、RREP 或 RERR 控制数据包，它应该创建路由条目作为
        适当给定控制中的序列号信息包，但不得转发任何控制包。如果节点
        接收到某个其他目的地的数据包，它应该如第 6.11 小节所述广播一个 RERR并且必须重置
        等待计时器在当前时间加上 DELETE_PERIOD 后到期。
        可以证明 [ 4 ] 到重新启动的节点退出时等待阶段并再次成为活动路由器，它的任何一个
        邻居将不再使用它作为活动的下一跳。它自己的一旦收到来自任何其他人的 RREQ，序列号就会更新
        节点，因为 RREQ 总是携带最大目标序列途中看到的号码。如果没有这样的 RREQ 到达，节点必须
        将自己的序列号初始化为零。
     */

}
