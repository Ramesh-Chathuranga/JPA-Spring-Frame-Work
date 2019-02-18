package lk.ijse.dep.app.business.custom.impl;

import lk.ijse.dep.app.business.Converter;
import lk.ijse.dep.app.business.custom.ManageOrdersBO;
import lk.ijse.dep.app.dao.custom.CustomerDAO;
import lk.ijse.dep.app.dao.custom.OrderDAO;
import lk.ijse.dep.app.dao.custom.OrderDetailDAO;
import lk.ijse.dep.app.dao.custom.QueryDAO;
import lk.ijse.dep.app.dto.OrderDTO;
import lk.ijse.dep.app.dto.OrderDTO2;
import lk.ijse.dep.app.dto.OrderDetailDTO;
import lk.ijse.dep.app.entity.CustomEntity;
import lk.ijse.dep.app.entity.Customer;
import lk.ijse.dep.app.entity.OrderDetail;
import lk.ijse.dep.app.entity.Orders;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
@Component
@Transactional
public class ManageOrdersBOImpl implements ManageOrdersBO {
    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private QueryDAO queryDAO;
    private CustomerDAO customerDAO;

    @Autowired
    public ManageOrdersBOImpl(OrderDAO orderDAO,OrderDetailDAO orderDetailDAO,QueryDAO queryDAO,CustomerDAO customerDAO) {
        this.orderDAO = orderDAO;
        this.orderDetailDAO = orderDetailDAO;
        this.queryDAO=queryDAO;
        this.customerDAO=customerDAO;
    }

    @Override
    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {

            List<CustomEntity> customEntities = queryDAO.findAllOrdersWithCustomerNameAndTotal().get();

            List<OrderDTO2>orderDTOS=new ArrayList<>();
            customEntities.forEach(ce -> {
                OrderDTO2 orderDTO2 = new OrderDTO2(ce.getOrderId(), ce.getOrderDate(), ce.getCustomerId(), ce.getCustomerName(), ce.getTotal());
                orderDTOS.add(orderDTO2);
            });
            return orderDTOS;


    }

    @Override
    public List<OrderDTO> getOrders() throws Exception {
           return null;
   }

    @Override
    public String generateOrderId() throws Exception {
            List<Orders> orders = orderDAO.findAll().get();

            final String[] lastID = {null};
            orders.forEach(orders1 -> {
                lastID[0] =orders1.getId();
               // System.out.println(orders1.getId());
            });
           return OrderID.generateOrderId(lastID);
    }

    @Override
    public void createOrder(OrderDTO dto) throws Exception {
            Customer customer = customerDAO.find(dto.getCustomerId()).get();
            Orders orders = new Orders(dto.getId(),dto.getDate(),customer);
            orderDAO.save(orders);
                dto.getOrderDetailDTOS().stream().forEach(odDTO -> {
                    OrderDetail orderDetail = new OrderDetail(dto.getId(),odDTO.getCode(),odDTO.getQty(),odDTO.getUnitPrice());
                    try {
                        orderDetailDAO.save(orderDetail);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    @Override
    public OrderDTO findOrder(String orderId) throws Exception {


            List<CustomEntity> customEntities = queryDAO.findOrderDetailsWithItemDescriptions(orderId).get();
            final String[] oid = {null};
            final String[] cid = {null};
            final LocalDate[] date = {null};
            List<OrderDetailDTO>details=new ArrayList<>();

            customEntities.forEach(cue -> {
                oid[0] =cue.getOrderId();
                cid[0] =cue.getCustomerId();
                date[0] =cue.getOrderDate();
                OrderDetailDTO orderDetailDTO = new OrderDetailDTO(cue.getItemCode(), cue.getDescription(), cue.getQty(), cue.getUnitPrice());
                 details.add(orderDetailDTO);
            });

            return new OrderDTO(oid[0],date[0],cid[0],details);


    }

//    private OrderDAO orderDAO;
//    private OrderDetailDAO orderDetailDAO;
//    private ItemDAO itemDAO;
//    private QueryDAO queryDAO;
//
//    public ManageOrdersBOImpl() {
//        orderDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER);
//        orderDetailDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER_DETAIL);
//        itemDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ITEM);
//        queryDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
//    }
//
//    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {
//
//        return queryDAO.findAllOrdersWithCustomerNameAndTotal().map(ce -> {
//            return Converter.getDTOList(ce, OrderDTO2.class);
//        }).get();
//
//    }
//
//    public List<OrderDTO> getOrders() throws Exception {
//
//        List<Orders> orders = orderDAO.findAll().get();
//        ArrayList<OrderDTO> tmpDTOs = new ArrayList<>();
//
//        for (Orders order : orders) {
//            List<OrderDetailDTO> tmpOrderDetailsDtos = queryDAO.findOrderDetailsWithItemDescriptions(order.getId()).map(ce -> {
//                return Converter.getDTOList(ce, OrderDetailDTO.class);
//            }).get();
//
//            OrderDTO dto = new OrderDTO(order.getId(),
//                    order.getDate().toLocalDate(),
//                    order.getCustomerId(), tmpOrderDetailsDtos);
//            tmpDTOs.add(dto);
//        }
//
//        return tmpDTOs;
//    }
//
//    public String generateOrderId() throws Exception {
//        return orderDAO.count() + 1 + "";
//    }
//
//    public void createOrder(OrderDTO dto) throws Exception {
//
//        DBConnection.getConnection().setAutoCommit(false);
//
//        try {
//
//            boolean result = orderDAO.save(new Orders(dto.getId(), Date.valueOf(dto.getDate()), dto.getCustomerId()));
//
//            if (!result) {
//                return;
//            }
//
//            for (OrderDetailDTO detailDTO : dto.getOrderDetailDTOS()) {
//                result = orderDetailDAO.save(new OrderDetail(dto.getId(),
//                        detailDTO.getCode(), detailDTO.getQty(), detailDTO.getUnitPrice()));
//
//                if (!result) {
//                    DBConnection.getConnection().rollback();
//                    return;
//                }
//
//                Item item = itemDAO.find(detailDTO.getCode()).get();
//                int qty = item.getQtyOnHand() - detailDTO.getQty();
//                item.setQtyOnHand(qty);
//                itemDAO.update(item);
//
//            }
//
//            DBConnection.getConnection().commit();
//
//        } catch (Exception ex) {
//            DBConnection.getConnection().rollback();
//            ex.printStackTrace();
//        } finally {
//            DBConnection.getConnection().setAutoCommit(true);
//        }
//
//    }
//
//    public OrderDTO findOrder(String orderId) throws Exception {
//        Orders order = orderDAO.find(orderId).get();
//
//        List<OrderDetailDTO> tmpOrderDetailsDtos = queryDAO.findOrderDetailsWithItemDescriptions(order.getId()).map(ce -> {
//            return Converter.getDTOList(ce, OrderDetailDTO.class);
//        }).get();
//
//        return new OrderDTO(order.getId(), order.getDate().toLocalDate(), order.getCustomerId(), tmpOrderDetailsDtos);
//    }
}
